"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { useState } from "react";
import { Pagination } from "@/components/Pagination";
import { RequireAuth } from "@/components/RequireAuth";
import { StatusBadge } from "@/components/StatusBadge";
import { api, ApiError } from "@/lib/api";
import { formatDate, formatPrice } from "@/lib/format";
import type { Order, Page, User } from "@/lib/types";
import { useAuth } from "@/store/auth";

export default function AccountPage() {
  return (
    <RequireAuth>
      <Account />
    </RequireAuth>
  );
}

function Account() {
  const [page, setPage] = useState(0);
  const orders = useQuery({ queryKey: ["orders", page], queryFn: () => api<Page<Order>>("/api/orders", { params: { page, size: 10 } }) });

  return (
    <div className="grid gap-8 lg:grid-cols-[1fr_340px]">
      <section>
        <h1 className="mb-4 text-2xl font-bold">Mes commandes</h1>
        {orders.isLoading && <p>Chargement…</p>}
        {orders.data?.content.length === 0 && <p className="text-slate-500">Vous n&apos;avez pas encore passé de commande.</p>}
        <div className="space-y-3">
          {orders.data?.content.map((o) => (
            <Link key={o.id} href={`/account/orders/${o.id}`} className="card flex items-center justify-between p-4 hover:border-brand-500">
              <div>
                <p className="font-semibold">Commande #{o.id.slice(0, 8)}</p>
                <p className="text-sm text-slate-500">{formatDate(o.createdAt)} · {o.items.length} article(s)</p>
              </div>
              <div className="flex items-center gap-4">
                <StatusBadge status={o.status} />
                <span className="font-bold">{formatPrice(o.totalCents)}</span>
              </div>
            </Link>
          ))}
        </div>
        <Pagination page={page} totalPages={orders.data?.totalPages ?? 0} onChange={setPage} />
      </section>
      <ProfileForm />
    </div>
  );
}

function ProfileForm() {
  const { user, setUser } = useAuth();
  const [firstName, setFirstName] = useState(user?.firstName ?? "");
  const [lastName, setLastName] = useState(user?.lastName ?? "");
  const [message, setMessage] = useState<string | null>(null);

  const save = useMutation({
    mutationFn: () => api<User>("/api/users/me", { method: "PUT", body: { firstName, lastName } }),
    onSuccess: (u) => {
      setUser(u);
      setMessage("Informations mises à jour ✓");
    },
    onError: (e) => setMessage(e instanceof ApiError ? e.message : "Erreur"),
  });

  return (
    <form
      className="card h-fit space-y-4 p-5"
      onSubmit={(e) => {
        e.preventDefault();
        save.mutate();
      }}
    >
      <h2 className="text-lg font-bold">Mes informations</h2>
      <div>
        <label className="label">E-mail</label>
        <input className="input bg-slate-100" disabled value={user?.email ?? ""} />
      </div>
      <div>
        <label className="label">Prénom</label>
        <input className="input" required value={firstName} onChange={(e) => setFirstName(e.target.value)} />
      </div>
      <div>
        <label className="label">Nom</label>
        <input className="input" required value={lastName} onChange={(e) => setLastName(e.target.value)} />
      </div>
      {message && <p className="text-sm text-slate-600">{message}</p>}
      <button className="btn-primary" disabled={save.isPending}>Enregistrer</button>
    </form>
  );
}
