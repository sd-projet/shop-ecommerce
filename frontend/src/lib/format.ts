const euro = new Intl.NumberFormat("fr-FR", { style: "currency", currency: "EUR" });

export const formatPrice = (cents: number) => euro.format(cents / 100);

export const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });

export const STATUS_LABELS: Record<string, string> = {
  PENDING: "En attente",
  PAID: "Payée",
  SHIPPED: "Expédiée",
  DELIVERED: "Livrée",
  CANCELLED: "Annulée",
};

export const SHIPPING_OPTIONS = [
  { value: "STANDARD", label: "Standard (3-5 jours)", cents: 490 },
  { value: "EXPRESS", label: "Express (24h)", cents: 990 },
  { value: "PICKUP", label: "Retrait en magasin", cents: 0 },
] as const;
