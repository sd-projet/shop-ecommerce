"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { useAuth } from "@/store/auth";
import { cartCount, useCart } from "@/store/cart";

const icon = "h-5 w-5";

function CartIcon() {
  return (
    <svg className={icon} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M3 4h2l2.4 11.2a1 1 0 0 0 1 .8h8.7a1 1 0 0 0 1-.8L20 8H6.2M9 20.5a.5.5 0 1 0 0 .01M17 20.5a.5.5 0 1 0 0 .01" />
    </svg>
  );
}

function SearchIcon() {
  return (
    <svg className={icon} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
      <circle cx="11" cy="11" r="6.5" />
      <path strokeLinecap="round" d="m16 16 4.5 4.5" />
    </svg>
  );
}

export function Navbar() {
  const router = useRouter();
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const items = useCart((s) => s.items);
  // persisted stores live in localStorage: only read them once mounted (avoids hydration mismatch)
  const [mounted, setMounted] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [userOpen, setUserOpen] = useState(false);
  const [query, setQuery] = useState("");
  const userMenu = useRef<HTMLDivElement>(null);
  useEffect(() => setMounted(true), []);

  // close menus on navigation / outside click
  useEffect(() => {
    setMenuOpen(false);
    setUserOpen(false);
  }, [pathname]);
  useEffect(() => {
    const onClick = (e: MouseEvent) => {
      if (userMenu.current && !userMenu.current.contains(e.target as Node)) setUserOpen(false);
    };
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  const count = mounted ? cartCount(items) : 0;
  const isAdmin = mounted && user?.role === "ROLE_ADMIN";
  const link = (href: string, label: string) => (
    <Link
      href={href}
      className={`rounded-lg px-3 py-2 text-sm font-medium transition ${
        pathname === href ? "bg-brand-50 text-brand-700" : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
      }`}
    >
      {label}
    </Link>
  );

  function search(e: React.FormEvent) {
    e.preventDefault();
    const q = query.trim();
    router.push(q ? `/?q=${encodeURIComponent(q)}` : "/");
    setMenuOpen(false);
  }

  function signOut() {
    logout();
    setUserOpen(false);
    router.push("/");
  }

  return (
    <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/90 backdrop-blur">
      <nav className="mx-auto flex max-w-6xl items-center gap-3 px-4 py-3">
        <Link href="/" className="flex items-center gap-2 text-xl font-extrabold text-slate-900">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-white">S</span>
          <span className="hidden sm:inline">Shop</span>
        </Link>

        <div className="ml-4 hidden items-center gap-1 md:flex">
          {link("/", "Catalogue")}
          {isAdmin && link("/admin", "Administration")}
        </div>

        <form onSubmit={search} className="relative mx-2 hidden flex-1 md:block md:max-w-md">
          <input
            className="input rounded-full bg-slate-100 pl-4 pr-10"
            placeholder="Rechercher un produit…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <button aria-label="Rechercher" className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-brand-600">
            <SearchIcon />
          </button>
        </form>

        <div className="ml-auto flex items-center gap-2">
          <Link href="/cart" aria-label="Panier" className="relative rounded-lg p-2 text-slate-700 hover:bg-slate-100">
            <CartIcon />
            {count > 0 && (
              <span className="absolute -right-0.5 -top-0.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-brand-600 px-1 text-xs font-bold text-white">
                {count}
              </span>
            )}
          </Link>

          {mounted && user ? (
            <div className="relative" ref={userMenu}>
              <button
                onClick={() => setUserOpen((v) => !v)}
                className="flex items-center gap-2 rounded-full border border-slate-200 py-1 pl-1 pr-3 text-sm font-medium hover:bg-slate-50"
              >
                <span className="flex h-7 w-7 items-center justify-center rounded-full bg-brand-100 text-xs font-bold uppercase text-brand-700">
                  {user.firstName.charAt(0)}
                  {user.lastName.charAt(0)}
                </span>
                <span className="hidden sm:inline">{user.firstName}</span>
              </button>
              {userOpen && (
                <div className="absolute right-0 mt-2 w-56 overflow-hidden rounded-xl border border-slate-200 bg-white py-1 shadow-lg">
                  <div className="border-b border-slate-100 px-4 py-2">
                    <p className="text-sm font-semibold">{user.firstName} {user.lastName}</p>
                    <p className="truncate text-xs text-slate-500">{user.email}</p>
                  </div>
                  <Link href="/account" className="block px-4 py-2 text-sm hover:bg-slate-50">Mes commandes & profil</Link>
                  {isAdmin && <Link href="/admin" className="block px-4 py-2 text-sm hover:bg-slate-50">Administration</Link>}
                  <button onClick={signOut} className="block w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-slate-50">
                    Déconnexion
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="hidden items-center gap-2 sm:flex">
              <Link href="/login" className="btn-secondary">Connexion</Link>
              <Link href="/register" className="btn-primary">Inscription</Link>
            </div>
          )}

          <button
            aria-label="Menu"
            className="rounded-lg p-2 text-slate-700 hover:bg-slate-100 md:hidden"
            onClick={() => setMenuOpen((v) => !v)}
          >
            <svg className={icon} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
              <path strokeLinecap="round" d={menuOpen ? "M6 6l12 12M18 6 6 18" : "M4 7h16M4 12h16M4 17h16"} />
            </svg>
          </button>
        </div>
      </nav>

      {menuOpen && (
        <div className="space-y-3 border-t border-slate-100 px-4 py-3 md:hidden">
          <form onSubmit={search}>
            <input className="input" placeholder="Rechercher un produit…" value={query} onChange={(e) => setQuery(e.target.value)} />
          </form>
          <div className="flex flex-col gap-1">
            {link("/", "Catalogue")}
            {link("/cart", "Panier")}
            {isAdmin && link("/admin", "Administration")}
            {mounted && !user && (
              <>
                {link("/login", "Connexion")}
                {link("/register", "Inscription")}
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
