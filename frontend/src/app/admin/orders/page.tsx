"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Pagination } from "@/components/Pagination";
import { StatusBadge } from "@/components/StatusBadge";
import { api, ApiError } from "@/lib/api";
import { STATUS_LABELS, formatDate, formatPrice } from "@/lib/format";
import type { Order, OrderStatus, Page } from "@/lib/types";

/** Transitions an administrator may trigger manually (PAID comes from the payment flow only). */
const NEXT: Record<OrderStatus, OrderStatus[]> = {
  PENDING: ["CANCELLED"],
  PAID: ["SHIPPED"],
  SHIPPED: ["DELIVERED"],
  DELIVERED: [],
  CANCELLED: [],
};

export default function AdminOrders() {
  const queryClient = useQueryClient();
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const orders = useQuery({
    queryKey: ["admin", "orders", status, page],
    queryFn: () => api<Page<Order>>("/api/admin/orders", { params: { status, page, size: 15 } }),
  });
  const update = useMutation({
    mutationFn: ({ id, status }: { id: string; status: OrderStatus }) => api<Order>(`/api/admin/orders/${id}/status`, { method: "PATCH", body: { status } }),
    onSuccess: () => {
      setError(null);
      queryClient.invalidateQueries({ queryKey: ["admin"] });
    },
    onError: (e) => setError(e instanceof ApiError ? e.message : "Erreur"),
  });

  return (
    <div className="space-y-4">
      <select className="input w-auto" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
        <option value="">Tous les statuts</option>
        {Object.entries(STATUS_LABELS).map(([value, label]) => (
          <option key={value} value={value}>{label}</option>
        ))}
      </select>
      {error && <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      <div className="card overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr><th className="p-3">Commande</th><th className="p-3">Date</th><th className="p-3">Client (livraison)</th><th className="p-3">Total</th><th className="p-3">Statut</th><th className="p-3">Action</th></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {orders.data?.content.map((o) => (
              <tr key={o.id}>
                <td className="p-3 font-mono">#{o.id.slice(0, 8)}</td>
                <td className="p-3">{formatDate(o.createdAt)}</td>
                <td className="p-3">{o.address.fullName}, {o.address.city}</td>
                <td className="p-3 font-semibold">{formatPrice(o.totalCents)}</td>
                <td className="p-3"><StatusBadge status={o.status} /></td>
                <td className="space-x-2 p-3">
                  {NEXT[o.status].map((next) => (
                    <button key={next} className={next === "CANCELLED" ? "btn-danger !py-1" : "btn-primary !py-1"} disabled={update.isPending} onClick={() => update.mutate({ id: o.id, status: next })}>
                      {next === "SHIPPED" ? "Expédier" : next === "DELIVERED" ? "Marquer livrée" : "Annuler"}
                    </button>
                  ))}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <Pagination page={page} totalPages={orders.data?.totalPages ?? 0} onChange={setPage} />
    </div>
  );
}
