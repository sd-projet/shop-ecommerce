"use client";

import { Elements, PaymentElement, useElements, useStripe } from "@stripe/react-stripe-js";
import { loadStripe } from "@stripe/stripe-js";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { RequireAuth } from "@/components/RequireAuth";
import { api, ApiError } from "@/lib/api";
import { SHIPPING_OPTIONS, formatPrice } from "@/lib/format";
import type { Address, CheckoutResponse, Order, ShippingMethod } from "@/lib/types";
import { cartTotal, useCart } from "@/store/cart";

const stripePromise = process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY
  ? loadStripe(process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY)
  : null;

const STEPS = ["Adresse", "Livraison", "Paiement"];

export default function CheckoutPage() {
  return (
    <RequireAuth>
      <Checkout />
    </RequireAuth>
  );
}

function Checkout() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { items, clear } = useCart();
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);

  const [step, setStep] = useState(0);
  const [address, setAddress] = useState<Address>({ fullName: "", street: "", city: "", postalCode: "", country: "France" });
  const [shipping, setShipping] = useState<ShippingMethod>("STANDARD");
  const [checkout, setCheckout] = useState<CheckoutResponse | null>(null);

  const shippingCost = SHIPPING_OPTIONS.find((o) => o.value === shipping)!.cents;
  const subtotal = cartTotal(items);

  const placeOrder = useMutation({
    mutationFn: () =>
      api<CheckoutResponse>("/api/orders", {
        method: "POST",
        body: {
          items: items.map((i) => ({ productId: i.productId, quantity: i.quantity })),
          address,
          shippingMethod: shipping,
        },
      }),
    onSuccess: (res) => {
      setCheckout(res);
      setStep(2);
    },
  });

  const finish = async (orderId: string) => {
    await api<Order>(`/api/orders/${orderId}/confirm-payment`, { method: "POST" });
    clear();
    queryClient.invalidateQueries({ queryKey: ["orders"] });
    router.push(`/account/orders/${orderId}`);
  };

  if (!mounted) return null;
  if (items.length === 0 && !checkout) {
    return (
      <div className="py-16 text-center">
        <p className="mb-4">Votre panier est vide.</p>
        <Link href="/" className="btn-primary">Retour au catalogue</Link>
      </div>
    );
  }

  return (
    <div className="grid gap-8 lg:grid-cols-[1fr_320px]">
      <div className="space-y-6">
        <ol className="flex gap-2 text-sm font-medium">
          {STEPS.map((label, i) => (
            <li key={label} className={`rounded-full px-4 py-1 ${i === step ? "bg-brand-600 text-white" : i < step ? "bg-brand-100 text-brand-700" : "bg-slate-200 text-slate-600"}`}>
              {i + 1}. {label}
            </li>
          ))}
        </ol>

        {step === 0 && (
          <form
            className="card space-y-4 p-6"
            onSubmit={(e) => {
              e.preventDefault();
              setStep(1);
            }}
          >
            <h2 className="text-lg font-bold">Adresse de livraison</h2>
            {(
              [
                ["fullName", "Nom complet"],
                ["street", "Adresse"],
                ["city", "Ville"],
                ["postalCode", "Code postal"],
                ["country", "Pays"],
              ] as const
            ).map(([key, label]) => (
              <div key={key}>
                <label className="label">{label}</label>
                <input className="input" required value={address[key]} onChange={(e) => setAddress({ ...address, [key]: e.target.value })} />
              </div>
            ))}
            <button className="btn-primary">Continuer</button>
          </form>
        )}

        {step === 1 && (
          <div className="card space-y-4 p-6">
            <h2 className="text-lg font-bold">Mode de livraison</h2>
            {SHIPPING_OPTIONS.map((o) => (
              <label key={o.value} className={`flex cursor-pointer items-center justify-between rounded-lg border p-4 ${shipping === o.value ? "border-brand-500 bg-brand-50" : "border-slate-200"}`}>
                <span className="flex items-center gap-3">
                  <input type="radio" checked={shipping === o.value} onChange={() => setShipping(o.value)} />
                  {o.label}
                </span>
                <span className="font-semibold">{o.cents === 0 ? "Gratuit" : formatPrice(o.cents)}</span>
              </label>
            ))}
            {placeOrder.isError && (
              <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700">
                {placeOrder.error instanceof ApiError ? placeOrder.error.message : "Erreur lors de la création de la commande"}
              </p>
            )}
            <div className="flex gap-3">
              <button className="btn-secondary" onClick={() => setStep(0)}>Retour</button>
              <button className="btn-primary" disabled={placeOrder.isPending} onClick={() => placeOrder.mutate()}>
                {placeOrder.isPending ? "Création…" : "Continuer vers le paiement"}
              </button>
            </div>
          </div>
        )}

        {step === 2 && checkout && <PaymentStep checkout={checkout} onPaid={() => finish(checkout.order.id)} />}
      </div>

      <aside className="card h-fit space-y-3 p-5">
        <h2 className="text-lg font-bold">Votre commande</h2>
        {(checkout?.order.items ?? items.map((i) => ({ productId: i.productId, productName: i.name, quantity: i.quantity, unitPriceCents: i.priceCents }))).map((i) => (
          <div key={i.productId} className="flex justify-between text-sm">
            <span>{i.quantity} × {i.productName}</span>
            <span>{formatPrice(i.quantity * i.unitPriceCents)}</span>
          </div>
        ))}
        <hr />
        <div className="flex justify-between text-sm"><span>Livraison</span><span>{shippingCost === 0 ? "Gratuit" : formatPrice(shippingCost)}</span></div>
        <div className="flex justify-between font-bold"><span>Total</span><span>{formatPrice(checkout?.order.totalCents ?? subtotal + shippingCost)}</span></div>
      </aside>
    </div>
  );
}

function PaymentStep({ checkout, onPaid }: { checkout: CheckoutResponse; onPaid: () => Promise<void> }) {
  const isFake = checkout.clientSecret.startsWith("fake_secret_") || !stripePromise;
  const options = useMemo(() => ({ clientSecret: checkout.clientSecret }), [checkout.clientSecret]);

  if (isFake) return <FakePayment onPaid={onPaid} />;
  return (
    <Elements stripe={stripePromise} options={options}>
      <StripeForm orderId={checkout.order.id} onPaid={onPaid} />
    </Elements>
  );
}

/** Used when the backend runs with the fake payment provider (no Stripe keys). */
function FakePayment({ onPaid }: { onPaid: () => Promise<void> }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  return (
    <div className="card space-y-4 p-6">
      <h2 className="text-lg font-bold">Paiement (simulation)</h2>
      <p className="rounded-lg bg-amber-50 p-3 text-sm text-amber-800">
        Mode démo : aucune clé Stripe configurée. Le paiement est simulé côté serveur.
      </p>
      {error && <p className="text-sm text-red-600">{error}</p>}
      <button
        className="btn-primary"
        disabled={loading}
        onClick={async () => {
          setLoading(true);
          try {
            await onPaid();
          } catch (e) {
            setError(e instanceof ApiError ? e.message : "Erreur de paiement");
            setLoading(false);
          }
        }}
      >
        {loading ? "Traitement…" : "Payer (simulation)"}
      </button>
    </div>
  );
}

function StripeForm({ orderId, onPaid }: { orderId: string; onPaid: () => Promise<void> }) {
  const stripe = useStripe();
  const elements = useElements();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!stripe || !elements) return;
    setLoading(true);
    setError(null);
    const result = await stripe.confirmPayment({
      elements,
      confirmParams: { return_url: `${window.location.origin}/account/orders/${orderId}?paid=1` },
      redirect: "if_required",
    });
    if (result.error) {
      setError(result.error.message ?? "Paiement refusé");
      setLoading(false);
      return;
    }
    try {
      await onPaid();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Erreur de validation du paiement");
      setLoading(false);
    }
  }

  return (
    <form onSubmit={submit} className="card space-y-4 p-6">
      <h2 className="text-lg font-bold">Paiement sécurisé</h2>
      <p className="text-xs text-slate-500">Mode test Stripe : carte 4242 4242 4242 4242, date future, CVC quelconque.</p>
      <PaymentElement />
      {error && <p className="text-sm text-red-600">{error}</p>}
      <button className="btn-primary" disabled={!stripe || loading}>{loading ? "Traitement…" : "Payer"}</button>
    </form>
  );
}
