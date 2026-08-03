# Workflow Improvement — DOD-FIX-06

- Terminal FE guard phải kiểm cả `disabled` và request count `0`, không chỉ nhìn màu nút.
- Luôn kèm Backend bypass probe để chứng minh defense in depth.
- Dùng synthetic fixture và hard cleanup thay vì để report terminal tích lũy trong test database.
- Có thể bổ sung component-test tooling sau này để test `canRequestAi` nhanh hơn, nhưng E2E vẫn cần
  giữ vì đây là acceptance behavior.
