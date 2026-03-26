#!/usr/bin/env python3
from __future__ import annotations

import json
import os
import smtplib
from datetime import datetime, timezone
from email.message import EmailMessage
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from typing import Any

HOST = os.getenv("FAKE_POSTMARK_HOST", "0.0.0.0")
PORT = int(os.getenv("FAKE_POSTMARK_PORT", "8081"))

SMTP_HOST = os.getenv("MAILHOG_SMTP_HOST", "mailhog")
SMTP_PORT = int(os.getenv("MAILHOG_SMTP_PORT", "1025"))

EXPECTED_TOKEN = os.getenv("POSTMARK_SERVER_TOKEN", "my-secret-token")

SENT_EMAILS: list[dict[str, Any]] = []


def json_response(handler: BaseHTTPRequestHandler, status: int, payload: dict[str, Any]) -> None:
    body = json.dumps(payload).encode("utf-8")
    handler.send_response(status)
    handler.send_header("Content-Type", "application/json")
    handler.send_header("Content-Length", str(len(body)))
    handler.end_headers()
    handler.wfile.write(body)


class FakePostmarkHandler(BaseHTTPRequestHandler):
    server_version = "FakePostmark/1.0"

    def do_GET(self) -> None:
        if self.path == "/health":
            json_response(self, HTTPStatus.OK, {"status": "ok"})
            return

        if self.path == "/sent-emails":
            json_response(self, HTTPStatus.OK, {"emails": SENT_EMAILS})
            return

        json_response(self, HTTPStatus.NOT_FOUND, {"error": "Not found"})

    def do_DELETE(self) -> None:
        if self.path == "/sent-emails":
            SENT_EMAILS.clear()
            self.send_response(HTTPStatus.NO_CONTENT)
            self.end_headers()
            return

        json_response(self, HTTPStatus.NOT_FOUND, {"error": "Not found"})

    def do_POST(self) -> None:
        if self.path != "/email":
            json_response(self, HTTPStatus.NOT_FOUND, {"error": "Not found"})
            return

        token = self.headers.get("X-Postmark-Server-Token")
        if token != EXPECTED_TOKEN:
            json_response(
                self,
                HTTPStatus.UNAUTHORIZED,
                {"ErrorCode": 10, "Message": "Invalid or missing X-Postmark-Server-Token"},
            )
            return

        content_type = self.headers.get("Content-Type", "")
        if "application/json" not in content_type:
            json_response(
                self,
                HTTPStatus.UNSUPPORTED_MEDIA_TYPE,
                {"ErrorCode": 11, "Message": "Content-Type must be application/json"},
            )
            return

        try:
            content_length = int(self.headers.get("Content-Length", "0"))
        except ValueError:
            json_response(
                self,
                HTTPStatus.BAD_REQUEST,
                {"ErrorCode": 12, "Message": "Invalid Content-Length"},
            )
            return

        raw_body = self.rfile.read(content_length)
        try:
            payload = json.loads(raw_body.decode("utf-8"))
        except json.JSONDecodeError:
            json_response(
                self,
                HTTPStatus.BAD_REQUEST,
                {"ErrorCode": 13, "Message": "Invalid JSON"},
            )
            return

        required_fields = ["From", "To", "Subject", "HtmlBody", "TextBody"]
        missing = [field for field in required_fields if not payload.get(field)]
        if missing:
            json_response(
                self,
                HTTPStatus.BAD_REQUEST,
                {"ErrorCode": 14, "Message": f"Missing required fields: {', '.join(missing)}"},
            )
            return

        try:
            self._send_to_mailhog(
                from_addr=str(payload["From"]),
                to_addr=str(payload["To"]),
                subject=str(payload["Subject"]),
                html_body=str(payload["HtmlBody"]),
                text_body=str(payload["TextBody"]),
            )
        except Exception as exc:
            json_response(
                self,
                HTTPStatus.BAD_GATEWAY,
                {"ErrorCode": 15, "Message": f"Failed to send to MailHog SMTP: {exc}"},
            )
            return

        record = {
            "From": payload["From"],
            "To": payload["To"],
            "Subject": payload["Subject"],
            "HtmlBody": payload["HtmlBody"],
            "TextBody": payload["TextBody"],
            "CapturedAt": datetime.now(timezone.utc).isoformat(),
        }
        SENT_EMAILS.append(record)

        json_response(
            self,
            HTTPStatus.OK,
            {
                "To": payload["To"],
                "SubmittedAt": datetime.now(timezone.utc).isoformat(),
                "MessageID": f"fake-{len(SENT_EMAILS)}",
                "ErrorCode": 0,
                "Message": "OK",
            },
        )

    def _send_to_mailhog(
        self,
        *,
        from_addr: str,
        to_addr: str,
        subject: str,
        html_body: str,
        text_body: str,
    ) -> None:
        msg = EmailMessage()
        msg["From"] = from_addr
        msg["To"] = to_addr
        msg["Subject"] = subject
        msg.set_content(text_body)
        msg.add_alternative(html_body, subtype="html")

        with smtplib.SMTP(SMTP_HOST, SMTP_PORT, timeout=10) as smtp:
            smtp.send_message(msg)

    def log_message(self, format: str, *args: Any) -> None:
        print(f"[{self.log_date_time_string()}] {self.address_string()} - {format % args}")


def main() -> None:
    server = ThreadingHTTPServer((HOST, PORT), FakePostmarkHandler)
    print(f"Fake Postmark listening on http://{HOST}:{PORT}")
    print(f"Forwarding mail to MailHog SMTP at {SMTP_HOST}:{SMTP_PORT}")
    print(f"Expected token: {EXPECTED_TOKEN}")
    server.serve_forever()


if __name__ == "__main__":
    main()