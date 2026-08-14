import { Award, Wallet } from "lucide-react-native";
import { Text } from "../../features/i18n/localized-native";
import { StyleSheet, View } from "react-native";

import { colors, spacing, typography } from "../../theme";
import {
  formatCurrentCompactNumber,
  formatCurrentCurrency,
  formatCurrentDate,
} from "../../features/i18n";
import type {
  ReviewerDashboardBadge,
  ReviewerDashboardPayout,
} from "../../types";

export type ReviewerPayoutHistoryRow = ReviewerDashboardPayout & {
  badge: ReviewerDashboardBadge | null;
};

type PayoutHistoryListProps = {
  rows: ReviewerPayoutHistoryRow[];
};

function formatVnd(value: number) {
  return formatCurrentCurrency(value);
}

function formatCount(value: number) {
  return formatCurrentCompactNumber(value);
}

function formatMonthLabel(value: string) {
  const [year, month] = value.split("-");
  const parsedMonth = Number(month);
  const parsedYear = Number(year);
  const date = new Date(parsedYear, parsedMonth - 1, 1);

  if (
    !year ||
    !month ||
    Number.isNaN(parsedMonth) ||
    Number.isNaN(parsedYear) ||
    Number.isNaN(date.getTime())
  ) {
    return value;
  }

  return formatCurrentDate(date, {
    month: "short",
    year: "numeric",
  });
}

function badgeLabel(badge: ReviewerDashboardBadge | null) {
  return `${badge ?? "IRON"} badge`;
}

function statusLabel(status: string) {
  return status.replace(/_/g, " ");
}

export function PayoutHistoryList({ rows }: PayoutHistoryListProps) {
  return (
    <View style={styles.list}>
      {rows.map((row) => (
        <PayoutHistoryRowCard item={row} key={row.id} />
      ))}
    </View>
  );
}

function PayoutHistoryRowCard({ item }: { item: ReviewerPayoutHistoryRow }) {
  return (
    <View style={styles.card}>
      <View style={styles.topRow}>
        <View style={styles.monthCopy}>
          <Text style={styles.metaLabel}>Month</Text>
          <Text style={styles.monthText}>{formatMonthLabel(item.payoutMonth)}</Text>
        </View>
        <View style={styles.statusPill}>
          <Text
            adjustsFontSizeToFit
            minimumFontScale={0.86}
            numberOfLines={1}
            style={styles.statusText}
          >
            {statusLabel(item.payoutStatus)}
          </Text>
        </View>
      </View>

      <View style={styles.badgeRow}>
        <View style={styles.badgeIcon}>
          <Award color={colors.primary} size={16} strokeWidth={2.4} />
        </View>
        <Text style={styles.badgeText}>{badgeLabel(item.badge)}</Text>
      </View>

      <View style={styles.amountBlock}>
        <View style={styles.amountLabelRow}>
          <Wallet color={colors.secondary} size={16} strokeWidth={2.4} />
          <Text style={styles.amountLabel}>Final amount</Text>
        </View>
        <Text
          adjustsFontSizeToFit
          minimumFontScale={0.82}
          numberOfLines={1}
          style={styles.amountText}
        >
          {formatVnd(item.totalAmount)}
        </Text>
      </View>

      <View style={styles.breakdownRows}>
        <BreakdownRow
          amount={item.likeAmount}
          count={item.likeCount}
          label="Likes"
        />
        <BreakdownRow
          amount={item.shareAmount}
          count={item.shareCount}
          label="Shares"
        />
        <BreakdownRow
          amount={item.commentAmount}
          count={item.commentCount}
          label="Comments"
        />
      </View>
    </View>
  );
}

function BreakdownRow({
  amount,
  count,
  label,
}: {
  amount: number;
  count: number;
  label: string;
}) {
  return (
    <View style={styles.breakdownRow}>
      <Text numberOfLines={1} style={styles.breakdownLabel}>
        {label} - {formatCount(count)}
      </Text>
      <Text
        adjustsFontSizeToFit
        minimumFontScale={0.82}
        numberOfLines={1}
        style={styles.breakdownAmount}
      >
        {formatVnd(amount)}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  amountBlock: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 8,
    gap: spacing.xs,
    padding: spacing.md,
  },
  amountLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "900",
    textTransform: "uppercase",
  },
  amountLabelRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.xs,
  },
  amountText: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "900",
  },
  badgeIcon: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 15,
    height: 30,
    justifyContent: "center",
    width: 30,
  },
  badgeRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
  },
  badgeText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
    textTransform: "capitalize",
  },
  breakdownAmount: {
    color: colors.foreground,
    flexShrink: 1,
    fontSize: typography.caption,
    fontWeight: "900",
    textAlign: "right",
  },
  breakdownLabel: {
    color: colors.muted,
    flex: 1,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  breakdownRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
    justifyContent: "space-between",
  },
  breakdownRows: {
    gap: spacing.sm,
  },
  card: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    gap: spacing.md,
    padding: spacing.lg,
  },
  list: {
    gap: spacing.md,
  },
  metaLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "900",
    textTransform: "uppercase",
  },
  monthCopy: {
    flex: 1,
    gap: spacing.xs,
  },
  monthText: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  statusPill: {
    alignItems: "center",
    backgroundColor: colors.tertiarySoft,
    borderRadius: 999,
    maxWidth: 132,
    minHeight: 30,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs,
  },
  statusText: {
    color: colors.tertiary,
    fontSize: typography.caption,
    fontWeight: "900",
    textTransform: "uppercase",
  },
  topRow: {
    alignItems: "flex-start",
    flexDirection: "row",
    gap: spacing.md,
    justifyContent: "space-between",
  },
});
