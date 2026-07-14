import unittest
from unittest.mock import call, patch


class SnapshotClientTest(unittest.TestCase):

    def test_iter_snapshot_pages_advances_with_since_and_source_id(self):
        pages = [
            {
                "items": [{"sourceId": "id-1"}],
                "nextSince": "2026-07-09T10:00:00",
                "nextSourceId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                "hasMore": True,
            },
            {
                "items": [],
                "nextSince": None,
                "nextSourceId": None,
                "hasMore": False,
            },
        ]
        import app.services.ingest.snapshot_client as snapshot_client

        with patch.object(snapshot_client, "get_rag_config") as mock_config, \
                patch.object(snapshot_client, "get_internal", side_effect=pages) as mock_get:
            mock_config.return_value = {"ingest": {"snapshot_page_limit": 2}}

            result = list(snapshot_client.iter_snapshot_pages("blog", None))

        self.assertEqual(result, pages)
        self.assertEqual(
            mock_get.call_args_list,
            [
                call("/api/internal/rag/snapshot", {"sourceType": "blog", "limit": 2}),
                call(
                    "/api/internal/rag/snapshot",
                    {
                        "sourceType": "blog",
                        "limit": 2,
                        "since": "2026-07-09T10:00:00",
                        "cursorId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                    },
                ),
            ],
        )


if __name__ == "__main__":
    unittest.main()
