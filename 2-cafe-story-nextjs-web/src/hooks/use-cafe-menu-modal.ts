"use client";

import { useState } from "react";

export function useCafeMenuModal() {
  const [isOpen, setIsOpen] = useState(false);

  return {
    closeMenu: () => setIsOpen(false),
    isOpen,
    openMenu: () => setIsOpen(true),
    setIsOpen,
  };
}
