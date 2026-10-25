"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Link from "next/link";
import { useParams, useSearchParams } from "next/navigation";
import { Suspense, useEffect } from "react";
import { RequireAuth } from "@/components/RequireAuth";
import { StatusBadge } from "@/components/StatusBadge";
import { api, ApiError } from "@/lib/api";
import { formatDate, formatPrice } from "@/lib/format";
import type { Order, OrderStatus } from "@/lib/types";
import { useCart } from "@/store/cart";

const TIMELINE: OrderStatus[] = ["PENDING", "PAID", "SHIPPED", "DELIVERED"];

export default function OrderPage() {
  return (
    <RequireAuth>
      <Suspense fallback={<p>Chargement…</p>}>
        <OrderDetail />
      </Suspense>
    </RequireAuth>
  );
}

function OrderDetail() {
  const { id } = useParams<{ id: string }>();
  const returnedFromStripe = useSearchParams().get("paid") === "1";
  const queryClient = useQueryClient();
  const clearCart = useCart((s) => s.clear);
  const { data: order, isLoading, isError } = useQuery({ queryKey: ["order", id], queryFn: () => api<Order>(`/api/orders/${id}`) });

  const refresh = (o: Order) => {
    queryClient.setQueryData(["order", id], o);
    queryClient.invalidateQueries({ queryKey: ["orders"] });
  };
  const confirm = useMutation({ mutationFn: () => api<Order>(`/api/orders/${id}/confirm-payment`, { method: "POST" }), onSuccess: refresh });
  const cancel = useMutation({ mutationFn: () => api<Order>(`/api/orders/${id}/cancel`, { method: "POST" }), onSuccess: refresh });

  // back from a Stripe redirect (3-D Secure...): ask the server to verify the payment
  useEffect(() => {
    if (returnedFromStripe && order?.status === "PENDING" && !confirm.isPending && !confirm.isError) {
      confirm.mutate();
      clearCart();
    }
  }, [returnedFromStripe, order?.status]); // eslint-disable-line react-hooks/exhaustive-deps

  if (isLoading) return <p>Chargement…</p>;
  if (isError || !order) return <p className="text-red-600">Commande introuvable.</p>;

  const currentIndex = TIMELINE.indexOf(order.status);

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <Link href="/account" className="text-sm text-brand-600">← Mes commandes</Link>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Commande #{order.id.slice(0, 8)}</h1>
        <StatusBadge status={order.status} />
      </div>
      <p className="text-sm text-slate-500">Passée le {formatDate(order.createdAt)}</p>

      {order.status !== "CANCELLED" && (
        <ol className="flex items-center gap-2 text-xs font-medium">
          {TIMELINE.map((s, i) => (
            <li key={s} className={`flex-1 rounded-full py-1 text-center ${i <= currentIndex ? "bg-brand-600 text-white" : "bg-slate-200 text-slate-600"}`}>
              {s === "PENDING" ? "En attente" : s === "PAID" ? "Payée" : s === "SHIPPED" ? "Expédiée" : "Livrée"}
            </li>
          ))}
        </ol>
      )}

      <div className="card divide-y divide-slate-100">
        {order.items.map((i) => (
          <div key={i.productId} className="flex justify-between p-4 text-sm">
            <span>{i.quantity} × {i.productName}</span>
            <span className="font-semibold">{formatPrice(i.quantity * i.unitPriceCents)}</span>
          </div>
        ))}
        <div className="flex justify-between p-4 text-sm"><span>Livraison</span><span>{formatPrice(order.shippingCents)}</span></div>
        <div className="flex justify-between p-4 font-bold"><span>Total</span><span>{formatPrice(order.totalCents)}</span></div>
      </div>

      <div className="card p-4 text-sm">
        <p className="mb-1 font-semibold">Adresse de livraison</p>
        <p>{order.address.fullName}</p>
        <p>{order.address.street}</p>
        <p>{order.address.postalCode} {order.address.city}, {order.address.country}</p>
      </div>

      {order.status === "PENDING" && (
        <div className="space-y-2">
          {(confirm.isError || cancel.isError) && (
            <p className="text-sm text-red-600">{((confirm.error ?? cancel.error) as ApiError).message}</p>
          )}
          <div className="flex gap-3">
            <button className="btn-primary" disabled={confirm.isPending} onClick={() => confirm.mutate()}>Vérifier le paiement</button>
            <button className="btn-danger" disabled={cancel.isPending} onClick={() => cancel.mutate()}>Annuler la commande</button>
          </div>
        </div>
      )}
    </div>
  );
}
