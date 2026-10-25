"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useAuth } from "@/store/auth";

export function RequireAuth({ children, admin = false }: { children: React.ReactNode; admin?: boolean }) {
  const router = useRouter();
  const user = useAuth((s) => s.user);
  // the persisted session lives in localStorage: only trust it once mounted on the client
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);

  useEffect(() => {
    if (!mounted) return;
    if (!user) router.replace("/login");
    else if (admin && user.role !== "ROLE_ADMIN") router.replace("/");
  }, [mounted, user, admin, router]);

  if (!mounted || !user || (admin && user.role !== "ROLE_ADMIN")) {
    return <p className="text-slate-500">Chargement…</p>;
  }
  return <>{children}</>;
}
