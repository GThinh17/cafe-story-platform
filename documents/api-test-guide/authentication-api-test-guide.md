# Authentication API Test Guide

Tài liệu này dùng để test nhanh module authentication hiện tại của CafeStory backend. Cơ chế auth đang dùng JWT lưu trong HttpOnly cookie, không dùng `Authorization: Bearer <token>` làm cơ chế chính.

## Mục Lục

- [Tổng Quan](#tổng-quan)
- [Chuẩn Bị Trước Khi Test](#chuẩn-bị-trước-khi-test)
- [Cookie Và CORS](#cookie-và-cors)
- [Response Format](#response-format)
- [1. Register](#1-register)
- [2. Login](#2-login)
- [3. Get Current User](#3-get-current-user)
- [4. Refresh Access Token](#4-refresh-access-token)
- [5. Logout](#5-logout)
- [6. Test Endpoint Cần Đăng Nhập](#6-test-endpoint-cần-đăng-nhập)
- [7. Test Role ADMIN](#7-test-role-admin)
- [Negative Test Cases](#negative-test-cases)
- [Postman Test Flow](#postman-test-flow)
- [curl Test Flow](#curl-test-flow)

## Tổng Quan

Base URL:

```text
http://localhost:8080
```

Auth endpoints:

| Method | Endpoint | Auth | Mô tả |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Tạo user mới, hash password, gán role mặc định `USER`. |
| `POST` | `/api/auth/login` | Public | Đăng nhập bằng email hoặc username, set `access_token` và `refresh_token` cookie. |
| `GET` | `/api/auth/me` | Cần `access_token` cookie | Lấy thông tin user hiện tại. |
| `POST` | `/api/auth/refresh` | Cần `refresh_token` cookie | Cấp lại `access_token`. |
| `POST` | `/api/auth/logout` | Cần `refresh_token` cookie nếu muốn revoke token | Revoke refresh token và xóa cookie. |

Security config hiện tại:

- Public:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `POST /api/auth/refresh`
  - Swagger/OpenAPI
- Cần authenticated:
  - Hầu hết API còn lại.
- Cần role `ADMIN`:
  - `/api/admin/**`
  - `/api/reviewers/payouts/**`
  - `/api/reviewers/badges/**`
  - `/api/reviewers/*/payouts`
  - `/api/reviewers/*/badges`

## Chuẩn Bị Trước Khi Test

Chạy backend:

```powershell
cd E:\LuanVanToTNghiep\cafe-story-platform\1-cafe-story-backend-javaspring
mvn spring-boot:run
```

Auth config trong `application.properties`:

```properties
app.jwt.secret=${JWT_SECRET:cafestory-dev-jwt-secret-change-me-at-least-32-bytes}
app.jwt.access-token-seconds=${JWT_ACCESS_TOKEN_SECONDS:900}
app.jwt.refresh-token-seconds=${JWT_REFRESH_TOKEN_SECONDS:604800}
app.auth.cookie.secure=${AUTH_COOKIE_SECURE:false}
app.auth.cookie.same-site=${AUTH_COOKIE_SAME_SITE:Lax}
```

Dev local nên giữ:

```text
AUTH_COOKIE_SECURE=false
AUTH_COOKIE_SAME_SITE=Lax
```

Nếu chạy production HTTPS, dùng:

```text
AUTH_COOKIE_SECURE=true
AUTH_COOKIE_SAME_SITE=Lax
```

## Cookie Và CORS

Cookie được set sau khi login:

| Cookie | Path | HttpOnly | Max age | Mô tả |
| --- | --- | --- | --- | --- |
| `access_token` | `/` | yes | Theo `app.jwt.access-token-seconds`, mặc định `900` giây | JWT access token. |
| `refresh_token` | `/api/auth` | yes | Theo `app.jwt.refresh-token-seconds`, mặc định `604800` giây | Refresh token raw; database chỉ lưu hash. |

Frontend web phải gọi API với credentials:

```ts
fetch("http://localhost:8080/api/auth/me", {
  credentials: "include"
});
```

CORS hiện cho phép:

```text
http://localhost:3000
http://localhost:8081
```

## Response Format

REST response được bọc bởi `GlobalResponseAdvice`:

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {}
}
```

Lưu ý quan trọng: `AuthResponse.accessToken` và `AuthResponse.refreshToken` đang có `@JsonIgnore`, nên token không xuất hiện trong response body. Token chỉ được gửi qua `Set-Cookie`.

## 1. Register

Tạo user mới và gán role mặc định `USER`.

```http
POST /api/auth/register
Content-Type: application/json
```

Body mẫu:

```json
{
  "userName": "auth_user_001",
  "userFullName": "Auth User 001",
  "password": "123456",
  "userEmail": "auth.user.001@example.com",
  "userPhone": 901234567,
  "userAvatar": "https://example.com/avatar-auth-user.png"
}
```

Expected status:

```text
201 Created
```

Response mẫu:

```json
{
  "statusCode": 201,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "user": {
      "userId": "11111111-1111-1111-1111-111111111111",
      "userName": "auth_user_001",
      "userFullName": "Auth User 001",
      "userEmail": "auth.user.001@example.com",
      "userPhone": 901234567,
      "userAvatar": "https://example.com/avatar-auth-user.png",
      "accountStatus": true,
      "roles": [
        "USER"
      ]
    }
  }
}
```

Ghi chú test:

- Register không set cookie login.
- Sau register, gọi tiếp `POST /api/auth/login`.
- Password được hash bằng `BCryptPasswordEncoder`.

## 2. Login

Đăng nhập bằng email hoặc username.

```http
POST /api/auth/login
Content-Type: application/json
```

Body mẫu khi dùng email:

```json
{
  "identifier": "auth.user.001@example.com",
  "password": "123456"
}
```

Body mẫu khi dùng username:

```json
{
  "identifier": "auth_user_001",
  "password": "123456"
}
```

Expected status:

```text
200 OK
```

Expected response headers:

```http
Set-Cookie: access_token=<jwt>; Path=/; Max-Age=900; HttpOnly; SameSite=Lax
Set-Cookie: refresh_token=<random-token>; Path=/api/auth; Max-Age=604800; HttpOnly; SameSite=Lax
```

Response body mẫu:

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "user": {
      "userId": "11111111-1111-1111-1111-111111111111",
      "userName": "auth_user_001",
      "userFullName": "Auth User 001",
      "userEmail": "auth.user.001@example.com",
      "userPhone": 901234567,
      "userAvatar": "https://example.com/avatar-auth-user.png",
      "accountStatus": true,
      "roles": [
        "USER"
      ]
    }
  }
}
```

Ghi chú test Postman:

- Sau login, Postman sẽ tự lưu cookie theo domain `localhost`.
- Mở tab `Cookies` của Postman để kiểm tra `access_token` và `refresh_token`.
- Các request sau không cần Bearer token.

## 3. Get Current User

Lấy thông tin user hiện tại từ `access_token` cookie.

```http
GET /api/auth/me
```

Headers/body:

```text
Không cần body.
Không cần Authorization header.
Request cần gửi kèm cookie access_token.
```

Expected status:

```text
200 OK
```

Response mẫu:

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "user": {
      "userId": "11111111-1111-1111-1111-111111111111",
      "userName": "auth_user_001",
      "userFullName": "Auth User 001",
      "userEmail": "auth.user.001@example.com",
      "userPhone": 901234567,
      "userAvatar": "https://example.com/avatar-auth-user.png",
      "accountStatus": true,
      "roles": [
        "USER"
      ]
    }
  }
}
```

## 4. Refresh Access Token

Cấp lại `access_token` bằng `refresh_token`.

```http
POST /api/auth/refresh
```

Headers/body:

```text
Không cần body.
Request cần gửi kèm cookie refresh_token.
```

Expected status:

```text
200 OK
```

Expected response header:

```http
Set-Cookie: access_token=<new-jwt>; Path=/; Max-Age=900; HttpOnly; SameSite=Lax
```

Response mẫu:

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "user": {
      "userId": "11111111-1111-1111-1111-111111111111",
      "userName": "auth_user_001",
      "userFullName": "Auth User 001",
      "userEmail": "auth.user.001@example.com",
      "userPhone": 901234567,
      "userAvatar": "https://example.com/avatar-auth-user.png",
      "accountStatus": true,
      "roles": [
        "USER"
      ]
    }
  }
}
```

Ghi chú:

- `refresh_token` không được rotate trong code hiện tại; API chỉ cấp lại `access_token`.
- Nếu `refresh_token` hết hạn, bị revoke hoặc không tồn tại, response là `401`.

## 5. Logout

Revoke refresh token và xóa cookie.

```http
POST /api/auth/logout
```

Headers/body:

```text
Không cần body.
Nên gửi kèm cookie refresh_token để backend revoke token trong database.
```

Expected status:

```text
200 OK
```

Expected response headers:

```http
Set-Cookie: access_token=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax
Set-Cookie: refresh_token=; Path=/api/auth; Max-Age=0; HttpOnly; SameSite=Lax
```

Response mẫu:

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": null
}
```

Sau logout, gọi lại:

```http
GET /api/auth/me
```

Expected:

```text
401 Unauthorized
```

## 6. Test Endpoint Cần Đăng Nhập

Sau khi login thành công, test một API bất kỳ cần authenticated, ví dụ:

```http
GET /api/users
```

Expected khi có `access_token` cookie:

```text
200 OK
```

Expected khi không có cookie:

```text
401 Unauthorized
```

## 7. Test Role ADMIN

Endpoint admin mẫu:

```http
POST /api/admin/blogs/{blogId}/ranking-override
```

User chỉ có role `USER` sẽ bị chặn:

```text
403 Forbidden
```

Để test role `ADMIN`, cần gán role trong database.

SQL mẫu:

```sql
INSERT INTO roles (name)
VALUES ('USER'), ('REVIEWER'), ('ADMIN'), ('CAFE_PAGE')
ON CONFLICT (name) DO NOTHING;

INSERT INTO user_roles (user_id, role_id, created_at)
SELECT '<USER_ID>'::uuid, id, NOW()
FROM roles
WHERE name = 'ADMIN'
ON CONFLICT DO NOTHING;
```

Sau khi gán role:

1. Logout.
2. Login lại để access token mới chứa role `ADMIN`.
3. Gọi lại endpoint admin.

## Negative Test Cases

### Register Thiếu Username

```http
POST /api/auth/register
Content-Type: application/json
```

Body:

```json
{
  "password": "123456",
  "userEmail": "missing.username@example.com"
}
```

Expected:

```text
400 Bad Request
```

Error mẫu:

```json
{
  "statusCode": 400,
  "status": "Fail",
  "message": "userName: Username is mandatory",
  "data": null
}
```

### Register Password Ngắn

Body:

```json
{
  "userName": "short_password_user",
  "password": "123",
  "userEmail": "short.password@example.com"
}
```

Expected:

```text
400 Bad Request
```

### Register Trùng Username

Gọi register hai lần với cùng `userName`.

Expected lần 2:

```text
409 Conflict
```

Message:

```text
Username already exists
```

### Register Trùng Email

Gọi register hai lần với cùng `userEmail`.

Expected lần 2:

```text
409 Conflict
```

Message:

```text
Email already exists
```

### Login Sai Password

```http
POST /api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "identifier": "auth.user.001@example.com",
  "password": "wrong-password"
}
```

Expected:

```text
401 Unauthorized
```

Message:

```text
Invalid username/email or password
```

### Me Không Có Cookie

```http
GET /api/auth/me
```

Expected:

```text
401 Unauthorized
```

### Refresh Không Có Cookie

```http
POST /api/auth/refresh
```

Expected:

```text
401 Unauthorized
```

Message:

```text
Refresh token is required
```

### Refresh Sau Logout

1. Login.
2. Logout.
3. Gọi `POST /api/auth/refresh` bằng refresh token cũ.

Expected:

```text
401 Unauthorized
```

Message:

```text
Refresh token was revoked
```

## Postman Test Flow

1. Tạo environment:

```text
base_url = http://localhost:8080
```

2. Register:

```http
POST {{base_url}}/api/auth/register
```

3. Login:

```http
POST {{base_url}}/api/auth/login
```

4. Kiểm tra Postman Cookies:

```text
access_token
refresh_token
```

5. Gọi me:

```http
GET {{base_url}}/api/auth/me
```

6. Refresh:

```http
POST {{base_url}}/api/auth/refresh
```

7. Logout:

```http
POST {{base_url}}/api/auth/logout
```

8. Gọi lại me để xác nhận logout:

```http
GET {{base_url}}/api/auth/me
```

Expected:

```text
401 Unauthorized
```

## curl Test Flow

Lưu cookie vào file:

```powershell
curl.exe -i -c cookies.txt -X POST "http://localhost:8080/api/auth/login" `
  -H "Content-Type: application/json" `
  -d "{\"identifier\":\"auth.user.001@example.com\",\"password\":\"123456\"}"
```

Gọi `/me` bằng cookie đã lưu:

```powershell
curl.exe -i -b cookies.txt "http://localhost:8080/api/auth/me"
```

Refresh access token và cập nhật cookie file:

```powershell
curl.exe -i -b cookies.txt -c cookies.txt -X POST "http://localhost:8080/api/auth/refresh"
```

Logout:

```powershell
curl.exe -i -b cookies.txt -c cookies.txt -X POST "http://localhost:8080/api/auth/logout"
```

Kiểm tra lại `/me` sau logout:

```powershell
curl.exe -i -b cookies.txt "http://localhost:8080/api/auth/me"
```

Expected:

```text
401 Unauthorized
```
