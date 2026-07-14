import hashlib
import hmac
import unittest
from unittest.mock import patch


class JavaClientHmacTest(unittest.TestCase):
    """Signature format must match RagHmacAuthFilter.java."""

    SECRET = "test-rag-secret"

    def _expected_signature(
        self, method: str, path: str, query: str, timestamp: str, body_hash: str
    ) -> str:
        payload = f"{method}\n{path}\n{query}\n{timestamp}\n{body_hash}"
        return hmac.new(
            self.SECRET.encode("utf-8"), payload.encode("utf-8"), hashlib.sha256
        ).hexdigest()

    def test_sign_matches_java_payload_format(self):
        import app.utils.java_client as java_client

        with patch.object(java_client, "RAG_INTERNAL_SECRET", self.SECRET):
            from app.utils.java_client import _body_hash, _sign

            body_hash = _body_hash()
            signature = _sign(
                "GET", "/api/internal/rag/snapshot", "sourceType=blog", "1751600000", body_hash
            )

        self.assertEqual(
            signature,
            self._expected_signature(
                "GET", "/api/internal/rag/snapshot", "sourceType=blog", "1751600000", body_hash
            ),
        )
        self.assertEqual(len(signature), 64)
        self.assertEqual(signature, signature.lower())

    def test_get_internal_raises_without_secret(self):
        import app.utils.java_client as java_client

        with patch.object(java_client, "RAG_INTERNAL_SECRET", ""):
            from app.utils.java_client import JavaClientError, get_internal

            with self.assertRaises(JavaClientError):
                get_internal("/api/internal/rag/snapshot")

    def test_get_internal_unwraps_format_response(self):
        import app.utils.java_client as java_client

        with patch.object(java_client, "RAG_INTERNAL_SECRET", self.SECRET), \
                patch.object(java_client.requests, "get") as mock_get:
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
        self.assertIn("X-RAG-Body-SHA256", sent_headers)

    def test_post_internal_signs_canonical_body_hash(self):
        import app.utils.java_client as java_client

        with patch.object(java_client, "RAG_INTERNAL_SECRET", self.SECRET), \
                patch.object(java_client.requests, "post") as mock_post:
            mock_post.return_value.status_code = 200
            mock_post.return_value.json.return_value = {"data": {"ok": True}}
            from app.utils.java_client import _body_hash, _canonical_json, post_internal

            data = post_internal(
                "/api/internal/user-context/blog-moderation",
                {"userJwt": "token-a", "limit": 5},
            )

        self.assertEqual(data, {"ok": True})
        sent_body = mock_post.call_args.kwargs["data"].decode("utf-8")
        self.assertEqual(sent_body, _canonical_json({"userJwt": "token-a", "limit": 5}))
        sent_headers = mock_post.call_args.kwargs["headers"]
        self.assertEqual(sent_headers["X-RAG-Body-SHA256"], _body_hash(sent_body))


if __name__ == "__main__":
    unittest.main()
