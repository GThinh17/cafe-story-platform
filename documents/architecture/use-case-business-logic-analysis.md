# 1. Tong quan he thong

CafeStory la he thong mang xa hoi ve quan ca phe va bai review, ket hop feed bai viet, ho so nguoi dung, trang quan ca phe, reviewer, tuong tac xa hoi, thanh toan goi dich vu, quang cao, bao cao noi dung, thong bao va chat.
Nguoi dung chinh gom guest, user da dang nhap, reviewer, chu/quan ly cafe page va admin.
Backend Spring Boot la noi co nghiep vu ro nhat; Next.js web hien co mot phan ket noi API that va nhieu man hinh UI/mock.
Mobile React Native hien chi la app Expo mac dinh, chua thay nghiep vu CafeStory.
He thong dung JWT trong HttpOnly cookie, refresh token, role USER/REVIEWER/ADMIN/CAFE_PAGE va chan `/api/admin/**` cho ADMIN.
Phan nen du lieu co user, role, region, blog, comment, cafe page, membership, follow/like/save/share/rating, report, notification, chat, payment, reviewer, ad campaign.

# 2. Danh sach actor tiem nang

| Actor | Vai tro trong he thong | Can cu tim thay trong code |
| ----- | ---------------------- | -------------------------- |
| Guest | Dang ky, dang nhap, lay goi y username; tren web bi redirect khi vao route duoc bao ve | `SecurityConfig` permit `/api/auth/register`, `/api/auth/login`, `/api/auth/usernames/suggestions`; web `proxy.ts` |
| User | Tai khoan da xac thuc, doc feed, quan ly ho so, viet blog, comment, like/save/share/rating, follow user/page, report, chat, notification | `UserRole.USER`, cac controller `/api/users`, `/api/blogs`, `/api/comments`, `/api/reports`, `/api/chat`, `/notifications` |
| Reviewer | User co role reviewer, co ho so reviewer, xep hang, thong ke, badge, payout | `UserRole.REVIEWER`, `ReviewerController`, `ReviewerServiceImpl`; mot so API payout/badge yeu cau ADMIN |
| Cafe Page Owner/Manager | Tao/quan ly cafe page, quan ly thanh vien page, dang bai dai dien page | `CafePage`, `PageMember`, `CafePageValidator.validateUserCanManagePage`, `validateUserCanCreateBlogOnPage` |
| Admin | Quan ly user, blog, comment, cafe page, report, moderation, payment, extra fee, dashboard, ranking override | `/api/admin/**` trong `SecurityConfig`; cac `Admin*Controller` |
| Payment Provider | Stripe/VNPAY goi webhook/return/IPN de cap nhat payment | `PaymentController`, `PaymentServiceImpl`; endpoint Stripe/VNPAY duoc permit public |
| Mobile User | Chua co actor nghiep vu that su | Mobile `App.tsx` chi la template Expo |

Ghi chu: role/permission co ro trong backend qua `UserRole`, `user_roles` va `SecurityConfig`. Web chua thay route admin rieng.

# 3. Nhom nghiep vu chinh cua he thong

## Authentication / Account

| Chuc nang | Mo ta logic nghiep vu | Actor lien quan | Trang thai |
| --------- | --------------------- | --------------- | ---------- |
| Dang ky | Tao user moi tu username/full name/email/password, tra auth response va cookie token | Guest | Co du frontend + backend |
| Dang nhap | Xac thuc bang email hoac username, cap access/refresh token trong cookie | Guest/User | Co du frontend + backend |
| Lay user hien tai | Doc principal JWT va tra thong tin user hien tai | User | Co du frontend + backend |
| Refresh token | Doi refresh token hop le lay token moi | User | Co backend, frontend co API wrapper |
| Dang xuat | Revoke refresh token va xoa cookie | User | Co du frontend + backend |
| Goi y username | Sinh username tu full name khi dang ky | Guest | Co du frontend + backend |
| Route guard web | Chan route `/blogs`, `/cafes`, `/explore`, `/messages`, `/notifications`, `/profile`, `/reviewers`, `/reviews` neu khong co access token | Guest/User | Co frontend, dua vao backend auth |

## User / Profile / Region

| Chuc nang | Mo ta logic nghiep vu | Actor lien quan | Trang thai |
| --------- | --------------------- | --------------- | ---------- |
| Xem danh sach user | Lay danh sach user, co thong tin tuong tac theo viewer neu co | User | Co backend, chua thay frontend |
| Xem ho so user | Lay user theo id | User | Co backend, frontend profile chu yeu mock/static |
| Cap nhat ho so ca nhan | Cap nhat avatar, full name, phone cua user hien tai | User | Co backend, frontend co form UI nhung chua thay goi API |
| Cap nhat region | Cap nhat dia chi/region cho user hien tai | User | Co backend, frontend co form UI nhung chua thay goi API |
| Xoa tai khoan ca nhan | Xoa user hien tai | User | Co backend, chua thay frontend |

## Post / Blog / Review

| Chuc nang | Mo ta logic nghiep vu | Actor lien quan | Trang thai |
| --------- | --------------------- | --------------- | ---------- |
| Tao blog/review | User active tao blog; neu gan cafe page thi phai la owner hoac ACTIVE OWNER/CO_OWNER cua page | User, Cafe Page Owner/Manager | Co backend, frontend co form tao review nhung chua thay goi API |
| Xem feed ca nhan | Lay feed theo ranking/recommendation, yeu cau dang nhap | User | Co du frontend + backend |
| Xem danh sach blog | Lay tat ca blog | User | Co backend, frontend co API wrapper nhung chua thay man hinh dung ro |
| Xem blog theo user | Lay blog cua mot author | User | Co backend, chua thay frontend |
| Xem chi tiet blog | Lay blog theo id, co trang thai like/save/rating theo viewer | User | Co backend, frontend blog detail dang dung mock data |
| Cap nhat blog | Chi author duoc update noi dung, anh, status, pinned, allowComment, page/region | User | Co backend, chua thay frontend |
| Xoa blog | Chi author duoc xoa blog | User | Co backend, chua thay frontend |
| Ghi nhan event blog | Ghi event VIEW/SAVE/REPORT... de phuc vu trending/recommendation | User/System | Co backend, chua thay frontend |
| Xem blog trending | Lay blog trending theo window | User | Co backend, frontend co API wrapper nhung chua thay man hinh dung ro |
| Rebuild recommendation feed | Tao lai cache recommendation cho user/toan he thong | Admin/System | Co backend, chua thay frontend |

## Comment / Reaction / Rating / Save / Share

| Chuc nang | Mo ta logic nghiep vu | Actor lien quan | Trang thai |
| --------- | --------------------- | --------------- | ---------- |
| Binh luan blog | Blog phai cho phep comment; co the reply comment cung blog; tang comment count | User | Co backend, frontend blog detail co UI comment nhung chua thay goi API |
| Xem comment blog/user/replies | Lay comment theo blog, user hoac parent comment | User | Co backend, frontend hien mock |
| Sua/xoa comment | Chi chu comment duoc sua/xoa; xoa giam comment count | User | Co backend, chua thay frontend |
| Like/unlike blog | User active like blog, chong trung lap, cap nhat counter | User | Co backend, frontend hien nut/so like nhung chua thay goi API |
| Save/unsave blog | Luu/bo luu blog, xem danh sach save theo blog/user/me | User | Co backend, frontend co tab saved/mock nhung chua thay goi API |
| Share blog | Chia se blog voi share type PUBLIC/PRIVATE/PAGE_ONLY, xem share theo blog/user | User | Co backend, frontend hien chi so share chua thay goi API |
| Rating blog | Danh gia/cap nhat/xoa rating, tinh average/count | User | Co backend, frontend form review co rating UI nhung chua thay goi API |

## Cafe Page / Region / Membership

| Chuc nang | Mo ta logic nghiep vu | Actor lien quan | Trang thai |
| --------- | --------------------- | --------------- | ---------- |
| Tao cafe page | User active tao page; moi user chi duoc lam owner mot page; tao owner/co-owner member ACTIVE | User, Cafe Page Owner | Co backend, chua thay frontend |
| Xem danh sach cafe page | Lay tat ca cafe page, co isFollowing/isLiked theo viewer | User | Co backend, frontend explore co top cafes API va fallback mock |
| Xem top cafe page | Xep hang active cafe page theo follower, like, do moi, loc region/city | User | Co du frontend + backend |
| Xem chi tiet cafe page | Lay page theo id | User | Co backend, frontend chi tiet cafe dang dung mock data |
| Xem blog cua cafe page | Lay blog published cua page bang cursor | User | Co backend, frontend chi tiet cafe dung mock review |
| Cap nhat/xoa cafe page | Chi owner hoac ACTIVE OWNER/CO_OWNER duoc quan ly page | Cafe Page Owner/Manager | Co backend, chua thay frontend |
| Join cafe page | User gui yeu cau lam MEMBER voi status PENDING | User | Co backend, chua thay frontend |
| Them thanh vien page | Page manager them user truc tiep, status ACTIVE, role OWNER/CO_OWNER/MEMBER | Cafe Page Owner/Manager | Co backend, chua thay frontend |
| Duyet/tu choi thanh vien | Page manager doi status ACTIVE hoac REJECTED | Cafe Page Owner/Manager | Co backend, chua thay frontend |
| Xem thanh vien/pending member | Lay danh sach member hoac pending request cua page | Cafe Page Owner/Manager | Co backend, chua thay frontend |
| Like/follow cafe page | User like/unlike va follow/unfollow page, xem follower/like | User | Co backend, frontend co nut UI nhung chua thay goi API |

## Reviewer / Ranking / Reward

| Chuc nang | Mo ta logic nghiep vu | Actor lien quan | Trang thai |
| --------- | --------------------- | --------------- | ---------- |
| Tao reviewer | Gan role REVIEWER va tao reviewer profile cho user | User/Admin | Co backend, chua thay frontend |
| Xem reviewer | Lay reviewer theo user id hoac danh sach reviewer | User | Co backend, frontend reviewer profile dang mock/static |
| Xem reviewer theo khu vuc | Loc reviewer theo city/province/ward/area/street, cham diem theo dia diem, review, follower, badge | User | Co backend, frontend chua thay goi API |
| Xem reviewer trending/top/ranking | Xep hang theo review, like, comment, share, save, follower, badge | User | Co backend, frontend chua thay goi API |
| Xem thong ke reviewer | Tinh like/share/comment/score theo period; self hoac admin moi xem | Reviewer/Admin | Co backend, chua thay frontend |
| Tao payout/badge hang thang | Admin tao payout va badge theo diem tuong tac; co chong duplicate neu khong overwrite | Admin | Co backend, chua thay frontend |
| Xem lich su payout/badge | Self reviewer hoac admin xem lich su | Reviewer/Admin | Co backend, chua thay frontend |
| Phan khuc reviewer/geo analytics | Gom reviewer theo segment va dia ly | Admin/User tuy endpoint | Co backend, chua thay frontend |

## Follow / Social

| Chuc nang | Mo ta logic nghiep vu | Actor lien quan | Trang thai |
| --------- | --------------------- | --------------- | ---------- |
| Follow/unfollow user | User active follow user khac, chan self-follow, cap nhat follower counter | User | Co backend, frontend profile co UI social/mock nhung chua thay goi API |
| Xem follower/following | Lay danh sach nguoi theo doi va dang theo doi | User | Co backend, frontend chua thay goi API |
| Following state trong reviewer | Response reviewer discovery co co `following` theo viewer | User | Co backend, chua thay frontend |

## Chat / Message

| Chuc nang | Mo ta logic nghiep vu | Actor lien quan | Trang thai |
| --------- | --------------------- | --------------- | ---------- |
| Tao direct conversation | Tao hoi thoai 1-1 | User | Co backend, frontend messages dung mock data |
| Tao group conversation | Tao nhom chat | User | Co backend, frontend messages dung mock data |
| Xem conversation/message | Lay danh sach conversation va message theo conversation | User | Co backend, frontend messages dung mock data |
| Gui message | Gui message text/image/sticker/mixed vao conversation | User | Co backend, frontend co form send UI nhung chua thay goi API |
| Quan ly member group | Them/xoa member, roi nhom, cap nhat thong tin group | User/Group member | Co backend, chua thay frontend |
| Socket chat | Co `ChatSocketController`, WebSocket config va Firebase sync service | User/System | Co backend, chua thay frontend goi socket |

## Notification

| Chuc nang | Mo ta logic nghiep vu | Actor lien quan | Trang thai |
| --------- | --------------------- | --------------- | ---------- |
| Tao notification | Service tao notification cho like/share/comment/message/follow | System | Co backend, chua thay frontend goi tao |
| Xem notification | Lay notification cua user co pagination/filter | User | Co backend, frontend notification page dung data UI/mock |
| Xem unread count | Dem thong bao chua doc | User | Co backend, chua thay frontend goi API |
| Mark read/read all/delete | Danh dau da doc, doc tat ca, xoa notification | User | Co backend, frontend chua thay goi API |
| Realtime notification | Emit WebSocket unread count va notification moi | System/User | Co backend, chua thay frontend subscribe |

## Report / Moderation / Admin Management

| Chuc nang | Mo ta logic nghiep vu | Actor lien quan | Trang thai |
| --------- | --------------------- | --------------- | ---------- |
| Bao cao noi dung | User active report BLOG/COMMENT/USER/CAFE_PAGE, chan tu report va duplicate active report; report blog ghi event penalty | User | Co backend, chua thay frontend |
| Quan ly report | Admin xem report, xem chi tiet, cap nhat status OPEN/REVIEWING/RESOLVED/REJECTED | Admin | Co backend, chua thay frontend |
| Hang doi moderation AI | Admin xem queue/result va resolve moderation result | Admin | Co backend, chua thay frontend |
| Quan ly user | Admin list/detail/update status/update roles | Admin | Co backend, chua thay frontend |
| Quan ly blog/comment/page | Admin list/detail/update status/delete | Admin | Co backend, chua thay frontend |
| Dashboard admin | Admin xem summary dashboard | Admin | Co backend, chua thay frontend |
| Ranking override blog | Admin tao override boost/pin cho blog ranking | Admin | Co backend, chua thay frontend |

## Payment / Subscription / Ads

| Chuc nang | Mo ta logic nghiep vu | Actor lien quan | Trang thai |
| --------- | --------------------- | --------------- | ---------- |
| Tao payment | User mua exactly one `extraFeeId` hoac `adFeeId`; ho tro STRIPE_CARD, BANK_TRANSFER, VNPAY; payment PENDING, het han 30 phut | User | Co backend, chua thay frontend |
| Xem payment | Buyer xem payment cua minh; admin xem payment bat ky | User/Admin | Co backend, chua thay frontend |
| Admin xem/refund/mark paid | Admin xem danh sach payment, mark bank transfer paid, refund | Admin | Co backend, chua thay frontend |
| Stripe webhook | Provider xac nhan checkout.session.completed de mark payment PAID | Payment Provider | Co backend, khong can frontend |
| VNPAY return/IPN | Provider tra ket qua/confirm giao dich, verify signature va amount | Payment Provider | Co backend, khong can frontend |
| Kich hoat reviewer package | Khi extra fee REVIEWER_REGISTRATION paid, tao/extend reviewer subscription va gan role REVIEWER | User/System | Co backend, chua thay frontend |
| Kich hoat cafe page package | Khi CAFE_PAGE_OPENING paid, tao/active cafe page package, owner membership, gan role CAFE_PAGE | User/System | Co backend, chua thay frontend |
| Quan ly extra fee | User da xac thuc xem active extra fees; admin CRUD/status extra fee | User/Admin | Co backend, chua thay frontend |
| Quan ly ad fee | Tao/list/delete ad fee | Admin/User tuy security chung | Co backend, chua thay frontend |
| Tao/quan ly ad campaign | Tao campaign khi payment ad da PAID va buyer la owner cafe page; xem theo cafe page, pause/activate, record click, auto expire | Cafe Page Owner/System/User | Co backend, chua thay frontend |
| Feed co ad slot | Feed service co sponsored cafe candidate/ad tracking | User/System | Co backend, frontend feed chi moi dung blog feed |

# 4. Danh sach use case de xuat

| Ma use case | Ten use case | Actor chinh | Actor phu | Mo ta ngan | Module |
| ----------- | ------------ | ----------- | --------- | ---------- | ------ |
| UC-01 | Dang ky | Guest | System | Tao tai khoan moi | Authentication |
| UC-02 | Dang nhap | Guest | System | Xac thuc va cap token | Authentication |
| UC-03 | Dang xuat | User | System | Thu hoi refresh token va xoa session | Authentication |
| UC-04 | Xem feed ca nhan | User | System | Xem bai viet goi y/ranking | Feed |
| UC-05 | Xem blog trending | User | System | Xem bai viet noi bat theo window | Blog |
| UC-06 | Tao blog/review | User | Cafe Page Owner/Manager | Tao bai viet ca nhan hoac bai viet cho page | Blog |
| UC-07 | Cap nhat blog | User |  | Sua bai viet cua minh | Blog |
| UC-08 | Xoa blog | User |  | Xoa bai viet cua minh | Blog |
| UC-09 | Xem chi tiet blog | User |  | Xem noi dung, like/save/rating state | Blog |
| UC-10 | Binh luan blog | User |  | Tao comment hoac reply neu blog cho comment | Comment |
| UC-11 | Quan ly comment cua minh | User |  | Sua/xoa comment cua minh | Comment |
| UC-12 | Like blog | User |  | Like/unlike bai viet | Reaction |
| UC-13 | Luu blog | User |  | Save/unsave bai viet | Reaction |
| UC-14 | Chia se blog | User |  | Tao ban ghi share blog | Reaction |
| UC-15 | Danh gia blog | User |  | Dat/xoa rating cho blog | Rating |
| UC-16 | Cap nhat ho so | User |  | Sua thong tin ca nhan va avatar | Profile |
| UC-17 | Cap nhat vi tri | User |  | Cap nhat region/dia chi | Profile |
| UC-18 | Theo doi nguoi dung | User |  | Follow/unfollow user khac | Follow |
| UC-19 | Tao cafe page | User |  | Tao page va owner/co-owner member | Cafe Page |
| UC-20 | Xem top cafe page | User | System | Xem page duoc xep hang theo khu vuc | Cafe Page |
| UC-21 | Cap nhat cafe page | Cafe Page Owner/Manager |  | Sua thong tin/status page | Cafe Page |
| UC-22 | Quan ly thanh vien page | Cafe Page Owner/Manager | User | Them/duyet/tu choi member | Cafe Page |
| UC-23 | Theo doi cafe page | User |  | Follow/unfollow page | Cafe Page |
| UC-24 | Like cafe page | User |  | Like/unlike page | Cafe Page |
| UC-25 | Dang bai tren cafe page | Cafe Page Owner/Manager | User | Tao blog dai dien cafe page | Blog/Cafe Page |
| UC-26 | Bao cao noi dung | User | Admin | Report blog/comment/user/cafe page | Report |
| UC-27 | Xu ly report | Admin |  | Cap nhat trang thai report | Admin Moderation |
| UC-28 | Xu ly moderation AI | Admin | System | Xem queue/result va resolve | Admin Moderation |
| UC-29 | Quan ly user | Admin |  | List/detail/status/role user | Admin |
| UC-30 | Quan ly bai viet | Admin |  | List/detail/status/delete blog | Admin |
| UC-31 | Quan ly comment | Admin |  | List/detail/status/delete comment | Admin |
| UC-32 | Quan ly cafe page | Admin |  | List/detail/status/delete page | Admin |
| UC-33 | Tao reviewer | User | System | Gan role reviewer va tao profile | Reviewer |
| UC-34 | Xem reviewer theo khu vuc | User | System | Tim reviewer theo region | Reviewer |
| UC-35 | Xem reviewer trending/top | User | System | Xem reviewer theo diem/badge/follower | Reviewer |
| UC-36 | Xem thong ke reviewer | Reviewer | Admin | Xem engagement score theo period | Reviewer |
| UC-37 | Tao payout reviewer | Admin | System | Tinh payout hang thang | Reviewer Reward |
| UC-38 | Tao badge reviewer | Admin | System | Tinh badge hang thang | Reviewer Reward |
| UC-39 | Tao payment | User | Payment Provider | Tao giao dich mua goi/ads | Payment |
| UC-40 | Xem payment | User | Admin | Xem payment cua minh hoac admin xem tat ca | Payment |
| UC-41 | Xac nhan payment | Payment Provider | System | Webhook/IPN/return cap nhat thanh toan | Payment |
| UC-42 | Quan ly payment | Admin |  | Mark paid/refund/list payment | Payment Admin |
| UC-43 | Mua goi reviewer | User | Payment Provider | Payment thanh cong kich hoat reviewer | Subscription |
| UC-44 | Mua goi cafe page | User | Payment Provider | Payment thanh cong kich hoat cafe page | Subscription |
| UC-45 | Quan ly extra fee | Admin |  | Tao/sua/status/xoa goi phi | Pricing |
| UC-46 | Tao chien dich quang cao | Cafe Page Owner | Payment Provider | Tao ad campaign bang payment ad da thanh toan | Ads |
| UC-47 | Tam dung/kich hoat campaign | Cafe Page Owner | System | Pause/activate/expire campaign | Ads |
| UC-48 | Ghi nhan click quang cao | User | System | Record click campaign | Ads |
| UC-49 | Tao hoi thoai | User | User | Tao direct/group conversation | Chat |
| UC-50 | Gui tin nhan | User | User | Gui message trong conversation | Chat |
| UC-51 | Quan ly nhom chat | User | User | Them/xoa member, roi nhom, sua group | Chat |
| UC-52 | Xem thong bao | User | System | Xem danh sach/unread count | Notification |
| UC-53 | Quan ly thong bao | User |  | Mark read/read all/delete | Notification |
| UC-54 | Ghi de xep hang blog | Admin | System | Boost/pin blog trong ranking | Admin Ranking |
| UC-55 | Xem dashboard admin | Admin | System | Xem tong quan he thong | Admin |

# 5. Quan he actor va chuc nang

Guest:

* Dang ky
* Dang nhap
* Lay goi y username
* Nhan redirect ve login khi vao route can auth tren web

User:

* Xem feed ca nhan, blog, top cafe page
* Tao/cap nhat/xoa blog cua minh
* Binh luan, reply, sua/xoa comment cua minh
* Like/save/share/rating blog
* Follow user, follow/like cafe page
* Cap nhat ho so va region
* Report blog/comment/user/cafe page
* Tao payment, xem payment cua minh
* Tao/xem conversation, gui tin nhan, quan ly nhom chat
* Xem/mark/xoa notification

Reviewer:

* Co reviewer profile
* Xem thong ke cua minh
* Xem lich su payout/badge cua minh
* Xuat hien trong top/trending/region discovery neu co du du lieu

Cafe Page Owner/Manager:

* Tao/cap nhat/xoa cafe page
* Quan ly thanh vien/pending request
* Dang blog cho cafe page neu owner hoac ACTIVE OWNER/CO_OWNER
* Tao/quan ly ad campaign

Admin:

* Quan ly user, roles, status
* Quan ly blog/comment/cafe page/report/moderation
* Tao ranking override cho blog
* Xem dashboard
* Quan ly payment, refund, mark bank transfer paid
* Quan ly extra fee
* Tao payout/badge reviewer hang thang

Payment Provider:

* Stripe webhook xac nhan thanh toan
* VNPAY return/IPN xac nhan thanh toan

# 6. Logic include / extend goi y

| Use case chinh | Quan he | Use case phu | Ly do |
| -------------- | ------- | ------------ | ----- |
| Dang ky | <<include>> | Goi y username | Frontend register goi suggestion theo full name truoc khi submit |
| Dang nhap | <<include>> | Tao access/refresh token | Login backend luon cap token/cookie |
| Dang xuat | <<include>> | Revoke refresh token | Logout backend thu hoi refresh token |
| Xem feed ca nhan | <<include>> | Xac thuc nguoi dung | Feed yeu cau authenticated request |
| Tao blog/review | <<include>> | Xac thuc nguoi dung | Backend dung actor user id tu JWT |
| Tao blog tren cafe page | <<include>> | Kiem tra quyen quan ly page | Backend yeu cau owner hoac ACTIVE OWNER/CO_OWNER |
| Cap nhat blog | <<include>> | Kiem tra chu blog | Chi author duoc cap nhat |
| Xoa blog | <<include>> | Kiem tra chu blog | Chi author duoc xoa |
| Binh luan blog | <<include>> | Kiem tra blog cho phep comment | Service chan comment neu `allowComment` false |
| Reply comment | <<extend>> | Binh luan blog | Chi xay ra khi co `parentCommentId` |
| Report blog | <<include>> | Ghi event REPORT | Report blog luon tao blog event weight am |
| Bao cao noi dung | <<include>> | Kiem tra khong tu report | Service chan report doi tuong cua chinh minh |
| Bao cao noi dung | <<include>> | Kiem tra duplicate active report | Service chan report trung OPEN/REVIEWING |
| Tao cafe page | <<include>> | Tao owner membership | Tao page luon tao PageMember cho owner/co-owner |
| Quan ly cafe page | <<include>> | Kiem tra quyen owner/co-owner | Update/delete/member management dung validator |
| Join cafe page | <<extend>> | Quan ly thanh vien page | Yeu cau join tao PENDING, sau do manager duyet/tu choi |
| Tao payment | <<include>> | Chon dung mot san pham mua | Service yeu cau exactly one extraFeeId/adFeeId |
| Xac nhan payment | <<include>> | Kich hoat san pham da mua | Payment PAID voi extra fee se active reviewer/cafe page package |
| Mua goi reviewer | <<include>> | Gan role REVIEWER | Khi thanh toan thanh cong gan role reviewer |
| Mua goi cafe page | <<include>> | Gan role CAFE_PAGE | Khi thanh toan thanh cong active cafe page package va role |
| Tao payout reviewer | <<include>> | Kiem tra role ADMIN | Endpoint/service yeu cau admin |
| Tao badge reviewer | <<include>> | Kiem tra role ADMIN | Endpoint/service yeu cau admin |
| Tao hoi thoai group | <<extend>> | Tao hoi thoai | Group conversation la bien the cua conversation |
| Mark read all notifications | <<extend>> | Quan ly notification | Chi xay ra khi user chon doc tat ca |

# 7. Ghi chu cho AI Agent ve Use Case Diagram

* So do tong quat nen gom actor: Guest, User, Reviewer, Cafe Page Owner/Manager, Admin, Payment Provider.
* System boundary nen gom: Authentication, User/Profile, Blog/Comment/Reaction, Cafe Page, Reviewer, Follow/Social, Chat, Notification, Report/Moderation, Payment/Subscription, Ads, Admin.
* Nen gop Blog + Comment + Reaction + Rating + Save + Share vao mot cum "Content Interaction" neu so do tong quat qua day.
* Nen gop Payment + Subscription + Extra Fee + Cafe Page/Reviewer activation vao cum "Payment & Packages".
* Nen tach Admin Management thanh so do rieng neu can the hien day du user/blog/comment/page/report/payment/moderation/dashboard/ranking override.
* Nen tach Chat/Notification thanh so do rieng neu can chi tiet socket/realtime.
* Khong nen dua mobile app vao so do chinh vi mobile hien chua co nghiep vu.
* Nen danh dau cac man hinh web mock/static la frontend chua hoan tat API: profile detail/reviews, create review submit, blog detail, cafe detail, messages, notifications.

# 8. Danh sach chuc nang chua ro hoac can hoi lai

| Chuc nang nghi ngo | Ly do chua ro | Nen hoi lai gi? |
| ------------------ | ------------- | --------------- |
| Admin web dashboard | Backend co nhieu admin API nhung web chua co route admin | Co du dinh lam admin tren Next.js hay dung cong cu rieng? |
| Forgot password | Link hien tren login UI nhung chua thay backend endpoint | Co can use case quen mat khau khong? |
| Google/Apple login | Nut UI co tren auth form nhung chua thay backend OAuth | Co can ve social login khong, hay bo khoi so do? |
| Create review submit tren web | Form UI co nut publish/save draft nhung khong goi API | Da co branch/frontend khac implement submit chua? |
| Notification frontend realtime | Backend co notification API/realtime, UI hien mock/static | Co can client subscribe WebSocket khong? |
| Chat frontend realtime | Backend co REST/socket chat, web messages dung mock | Co can dua realtime chat vao so do chinh hay de backend-only? |
| Mobile app | Mobile chi la template Expo | Mobile co nam trong pham vi Use Case Diagram khong? |
| AI moderation tu dong | Co entity/service admin moderation, chua thay flow tu dong tao moderation result | AI moderation duoc goi tu dau trong runtime? |
| Region management | Co entity/repository va user/page region DTO, nhung chua thay controller CRUD region rieng | Region la lookup co san hay admin quan ly? |
