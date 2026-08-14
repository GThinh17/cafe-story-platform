import {
  forwardRef,
  type ComponentRef,
  type ReactNode,
} from "react";
import {
  Alert as NativeAlert,
  Pressable as NativePressable,
  Switch as NativeSwitch,
  Text as NativeText,
  TextInput as NativeTextInput,
  type AlertButton,
  type AlertOptions,
  type PressableProps,
  type SwitchProps,
  type TextInputProps,
  type TextProps,
} from "react-native";

import { getCurrentLocale } from "./current-locale";
import { useI18n } from "./locale-provider";
import { translateUiText } from "./ui-phrases";

function localizeNode(node: ReactNode, locale: "en" | "vi"): ReactNode {
  if (typeof node === "string") {
    return translateUiText(locale, node);
  }

  if (Array.isArray(node)) {
    return node.map((child) => localizeNode(child, locale));
  }

  return node;
}

export const Text = forwardRef<ComponentRef<typeof NativeText>, TextProps>(
  function LocalizedText(
    { accessibilityHint, accessibilityLabel, children, ...props },
    ref,
  ) {
    const { locale } = useI18n();

    return (
      <NativeText
        {...props}
        accessibilityHint={
          accessibilityHint
            ? translateUiText(locale, accessibilityHint)
            : undefined
        }
        accessibilityLabel={
          accessibilityLabel
            ? translateUiText(locale, accessibilityLabel)
            : undefined
        }
        ref={ref}
      >
        {localizeNode(children, locale)}
      </NativeText>
    );
  },
);

export const Pressable = forwardRef<
  ComponentRef<typeof NativePressable>,
  PressableProps
>(function LocalizedPressable(
  { accessibilityHint, accessibilityLabel, ...props },
  ref,
) {
  const { locale } = useI18n();

  return (
    <NativePressable
      {...props}
      accessibilityHint={
        accessibilityHint
          ? translateUiText(locale, accessibilityHint)
          : undefined
      }
      accessibilityLabel={
        accessibilityLabel
          ? translateUiText(locale, accessibilityLabel)
          : undefined
      }
      ref={ref}
    />
  );
});

export const TextInput = forwardRef<
  ComponentRef<typeof NativeTextInput>,
  TextInputProps
>(function LocalizedTextInput(
  { accessibilityHint, accessibilityLabel, placeholder, ...props },
  ref,
) {
  const { locale } = useI18n();

  return (
    <NativeTextInput
      {...props}
      accessibilityHint={
        accessibilityHint
          ? translateUiText(locale, accessibilityHint)
          : undefined
      }
      accessibilityLabel={
        accessibilityLabel
          ? translateUiText(locale, accessibilityLabel)
          : undefined
      }
      placeholder={
        placeholder ? translateUiText(locale, placeholder) : undefined
      }
      ref={ref}
    />
  );
});

export const Switch = forwardRef<
  ComponentRef<typeof NativeSwitch>,
  SwitchProps
>(function LocalizedSwitch(
  { accessibilityHint, accessibilityLabel, ...props },
  ref,
) {
  const { locale } = useI18n();

  return (
    <NativeSwitch
      {...props}
      accessibilityHint={
        accessibilityHint
          ? translateUiText(locale, accessibilityHint)
          : undefined
      }
      accessibilityLabel={
        accessibilityLabel
          ? translateUiText(locale, accessibilityLabel)
          : undefined
      }
      ref={ref}
    />
  );
});

function localizeAlertButtons(
  buttons: AlertButton[] | undefined,
): AlertButton[] | undefined {
  if (!buttons) {
    return undefined;
  }

  const locale = getCurrentLocale();
  return buttons.map((button) => ({
    ...button,
    text: button.text ? translateUiText(locale, button.text) : button.text,
  }));
}

export const Alert = {
  alert(
    title: string,
    message?: string,
    buttons?: AlertButton[],
    options?: AlertOptions,
  ) {
    const locale = getCurrentLocale();
    NativeAlert.alert(
      translateUiText(locale, title),
      message ? translateUiText(locale, message) : undefined,
      localizeAlertButtons(buttons),
      options,
    );
  },
};

export function translateCurrentUiText(value: string): string {
  return translateUiText(getCurrentLocale(), value);
}
