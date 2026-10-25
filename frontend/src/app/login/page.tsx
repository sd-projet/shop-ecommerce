"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { AuthResponse } from "@/lib/types";
import { useAuth } from "@/store/auth";

export default function LoginPage() {
  const router = useRouter();
  const setSession = useAuth((s) => s.setSession);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await api<AuthResponse>("/api/auth/login", { method: "POST", body: { email, password } });
      setSession(res.token, res.user);
      router.push(res.user.role === "ROLE_ADMIN" ? "/admin" : "/");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Connexion impossible");
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={submit} className="card mx-auto max-w-md space-y-4 p-6">
      <h1 className="text-2xl font-bold">Connexion</h1>
      {error && <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}
      <div>
        <label className="label">E-mail</label>
        <input className="input" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
      </div>
      <div>
        <label className="label">Mot de passe</label>
        <input className="input" type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
      </div>
      <button className="btn-primary w-full" disabled={loading}>{loading ? "Connexion…" : "Se connecter"}</button>
      <p className="text-center text-sm text-slate-500">
        Pas de compte ? <Link href="/register" className="font-medium text-brand-600">Inscrivez-vous</Link>
      </p>
      <p className="rounded-lg bg-slate-50 p-3 text-xs text-slate-500">Démo admin : admin@shop.com / Admin123!</p>
    </form>
  );
}
