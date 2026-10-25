"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { RequireAuth } from "@/components/RequireAuth";

const LINKS = [
  { href: "/admin", label: "Dashboard" },
  { href: "/admin/products", label: "Produits & stocks" },
  { href: "/admin/orders", label: "Commandes" },
];

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  return (
    <RequireAuth admin>
      <div className="mb-6 flex gap-2 border-b border-slate-200 pb-3">
        {LINKS.map((l) => (
          <Link key={l.href} href={l.href} className={`rounded-lg px-4 py-2 text-sm font-medium ${pathname === l.href ? "bg-brand-600 text-white" : "hover:bg-slate-200"}`}>
            {l.label}
          </Link>
        ))}
      </div>
      {children}
    </RequireAuth>
  );
}
