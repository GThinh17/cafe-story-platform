"use client";

import { CheckCircle2, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogTitle,
} from "@/components/ui/dialog";
import { cn } from "@/lib/utils";

type PricingPlanModalProps = {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
};

type MembershipPlan = {
  audience: string;
  cta: string;
  features: string[];
  highlighted?: boolean;
  name: string;
  price: string;
};

const membershipPlans: MembershipPlan[] = [
  {
    audience: "Cá nhân",
    cta: "Đăng ký ngay",
    features: [
      "Nhận badge Reviewer Pro hiển thị trên hồ sơ và bài review",
      "Được ưu tiên xuất hiện trong danh sách reviewer nổi bật của CafeStory",
      "Xem thống kê lượt xem, lượt lưu và tương tác cho từng bài review",
      "Nhận tiền thưởng từ lượt xem và tương tác trên bài review theo chính sách của CafeStory",
      "Tham gia trải nghiệm menu mới, tasting event và ưu đãi từ quán đối tác",
    ],
    name: "Hội Viên Reviewer",
    price: "199.000đ",
  },
  {
    audience: "Kinh doanh",
    cta: "Đăng ký ngay",
    features: [
      "Xác minh hồ sơ quán và gắn badge Official để tăng độ tin cậy",
      "Cập nhật menu, giờ mở cửa, ảnh không gian và thông tin đặt bàn",
      "Tạo quảng cáo ưu đãi để tiếp cận reviewer và khách hàng quanh khu vực",
      "Xem báo cáo lượt xem hồ sơ, lượt lưu quán và nguồn khách quan tâm",
      "Thêm tối đa 1 thành viên cùng quản lý hồ sơ quán và tương tác với reviewer",
    ],
    highlighted: true,
    name: "Gói Chủ Quán",
    price: "499.000đ",
  },
];

export function PricingPlanModal({
  isOpen,
  onOpenChange,
}: PricingPlanModalProps) {
  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[min(95vh,860px)] w-[min(96vw,860px)] max-w-[900px] overflow-y-auto border-[#e1d7c8] bg-[#fffdf8] px-6 pb-7 pt-6 text-[#271310] sm:px-8">
        <DialogClose asChild>
          <Button
            aria-label="Đóng bảng gói hội viên"
            className="absolute right-4 top-4 text-[#271310] hover:bg-[#efe7da]"
            size="icon-sm"
            type="button"
            variant="ghost"
          >
            <X />
          </Button>
        </DialogClose>

        <header className="mx-auto flex max-w-[700px] flex-col items-center gap-3 pr-7 text-center">
          <DialogTitle className="font-heading text-3xl font-bold leading-[1.08] text-[#271310] sm:text-4xl">
            Nâng Tầm Trải Nghiệm Cà Phê
          </DialogTitle>
          <p className="text-sm leading-6 text-[#504442]">
            Chọn gói hội viên phù hợp để bắt đầu hành trình chuyên nghiệp cùng
            CafeStory.
          </p>
        </header>

        <div className="mt-6 grid gap-4 md:grid-cols-2">
          {membershipPlans.map((plan) => (
            <article
              className={cn(
                "flex min-h-[500px] flex-col rounded-sm border p-7 shadow-sm",
                plan.highlighted
                  ? "border-[#271310] bg-[#271310] text-white shadow-[0_24px_48px_rgba(39,19,16,0.28)]"
                  : "border-[#d3c3c0] bg-[#fffdf8] text-[#271310]",
              )}
              key={plan.name}
            >
              <span
                className={cn(
                  "w-fit rounded-full px-3 py-1 text-[10px] font-black uppercase tracking-[0.14em]",
                  plan.highlighted
                    ? "bg-white/12 text-[#d9bbb4]"
                    : "bg-[#d3c3c0]/70 text-[#504442]",
                )}
              >
                {plan.audience}
              </span>

              <h3 className="mt-2 font-serif text-2xl leading-tight">
                {plan.name}
              </h3>

              <p className="mt-5 flex items-end gap-1">
                <span className="font-serif text-3xl font-bold leading-none">
                  {plan.price}
                </span>
                <span
                  className={cn(
                    "text-sm",
                    plan.highlighted ? "text-white/72" : "text-[#504442]",
                  )}
                >
                  /tháng
                </span>
              </p>

              <ul className="mt-8 flex flex-1 flex-col gap-3.5">
                {plan.features.map((feature) => (
                  <li
                    className="grid grid-cols-[18px_1fr] gap-3 text-sm leading-5"
                    key={feature}
                  >
                    <CheckCircle2
                      aria-hidden="true"
                      className={cn(
                        "mt-0.5",
                        plan.highlighted ? "text-[#f0d6d0]" : "text-[#b49a93]",
                      )}
                    />
                    <span
                      className={cn(
                        plan.highlighted ? "text-white/88" : "text-[#504442]",
                      )}
                    >
                      {feature}
                    </span>
                  </li>
                ))}
              </ul>

              <Button
                className={cn(
                  "mt-9 h-12 w-full font-black",
                  plan.highlighted
                    ? "bg-white text-[#271310] hover:bg-[#efe7da]"
                    : "bg-[#271310] text-white hover:bg-[#40221d]",
                )}
                type="button"
              >
                {plan.cta}
              </Button>
            </article>
          ))}
        </div>
      </DialogContent>
    </Dialog>
  );
}
