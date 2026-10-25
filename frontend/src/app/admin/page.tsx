"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { STATUS_LABELS, formatPrice } from "@/lib/format";
import type { Dashboard } from "@/lib/types";

export default function AdminDashboard() {
  const { data, isLoading } = useQuery({ queryKey: ["admin", "dashboard"], queryFn: () => api<Dashboard>("/api/admin/dashboard") });
  if (isLoading || !data) return <p>Chargement…</p>;

  const kpis = [
    { label: "Chiffre d'affaires", value: formatPrice(data.revenueCents) },
    { label: "Commandes", value: data.totalOrders },
    { label: "Clients", value: data.customers },
    { label: "Produits actifs", value: data.activeProducts },
    { label: "Stock faible (≤5)", value: data.lowStockProducts },
  ];

  return (
    <div className="space-y-8">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        {kpis.map((k) => (
          <div key={k.label} className="card p-5">
            <p className="text-sm text-slate-500">{k.label}</p>
            <p className="mt-1 text-2xl font-bold">{k.value}</p>
          </div>
        ))}
      </div>
      <div className="card p-5">
        <h2 className="mb-3 font-bold">Commandes par statut</h2>
        <div className="grid gap-3 sm:grid-cols-5">
          {Object.entries(data.ordersByStatus).map(([status, count]) => (
            <div key={status} className="rounded-lg bg-slate-50 p-3 text-center">
              <p className="text-xl font-bold">{count}</p>
              <p className="text-xs text-slate-500">{STATUS_LABELS[status]}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
