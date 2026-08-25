# Assumptions Register

| ID | Assumption | Validation/guard |
|---|---|---|
| `AS-001` | Admin app remains `5-cafe-story-nextjs-admin` | Recheck checkout before code |
| `AS-002` | Backend remains final authority | Enforced by architecture/tests |
| `AS-003` | n8n is single active instance for local nonce cache | Multi-instance requires shared store |
| `AS-004` | BLOG/COMMENT fields can produce minimum snapshot | Verify entities/repositories in implementation |
| `AS-005` | V2 migration can be additive | Read-only schema/Flyway check first |
| `AS-006` | Existing endpoint path remains compatible | Contract/controller tests |
| `AS-007` | Safe BLOG/COMMENT fixtures exist or can be approved | Otherwise `TEST_DATA/BLOCKED` |
| `AS-008` | HMAC crypto is permitted in n8n runtime | Runtime preflight; blocker if unavailable |
| `AS-009` | Legal/privacy owner may revise retention | Required before production |

Assumption is not evidence. Failed assumption reopens design/implementation scope and must not be
hidden by weakening tests.
