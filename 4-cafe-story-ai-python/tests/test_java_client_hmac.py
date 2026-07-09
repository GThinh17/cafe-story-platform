import hashlib
import hmac
import unittest
from unittest.mock import patch


class JavaClientHmacTest(unittest.TestCase):
    """Signature format phải khớp RagHmacAuthFilter.java:
    HMAC_SHA256(secret, method \n path \n query \n timestamp) hex lowercase.
    """

    SECRET = "test-rag-secret"

    def _expected_signature(self, method: str, path: str, query: str, timestamp: str) -> str:
        payload = f"{method}\n{path}\n{query}\n{timestamp}"
        return hmac.new(
            self.SECRET.encode("utf-8"), payload.encode("utf-8"), hashlib.sha256
        ).hexdigest()

    def test_sign_matches_java_payload_format(self):
        with patch("app.utils.java_client.RAG_INTERNAL_SECRET", self.SECRET):
            from app.utils.java_client import _sign

            signature = _sign("GET", "/api/internal/rag/snapshot", "sourceType=blog", "1751600000")

        self.assertEqual(
            signature,
            self._expected_signature(
                "GET", "/api/internal/rag/snapshot", "sourceType=blog", "1751600000"
            ),
        )
        self.assertEqual(len(signature), 64)
        self.assertEqual(signature, signature.lower())

    def test_get_internal_raises_without_secret(self):
        with patch("app.utils.java_client.RAG_INTERNAL_SECRET", ""):
            from app.utils.java_client import JavaClientError, get_internal

            with self.assertRaises(JavaClientError):
                get_internal("/api/internal/rag/snapshot")

    def test_get_internal_unwraps_format_response(self):
        with patch("app.utils.java_client.RAG_INTERNAL_SECRET", self.SECRET), \
                patch("app.utils.java_client.requests.get") as mock_get:
            mock_get.return_value.status_code = 200
            mock_get.return_value.json.return_value = {
                "statusCode": 200,
                "status": "OK",
                "message": "success",
                "data": {"items": [], "tombstones": [], "hasMore": False},
            }
            from app.utils.java_client import get_internal

            data = get_internal("/api/internal/rag/snapshot", {"sourceType": "blog"})

        self.assertEqual(data, {"items": [], "tombstones": [], "hasMore": False})
        sent_headers = mock_get.call_args.kwargs["headers"]
        self.assertIn("X-RAG-Signature", sent_headers)
        self.assertIn("X-RAG-Timestamp", sent_headers)


if __name__ == "__main__":
    unittest.main()
