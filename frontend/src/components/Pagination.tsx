export function Pagination({ page, totalPages, onChange }: { page: number; totalPages: number; onChange: (p: number) => void }) {
  if (totalPages <= 1) return null;
  return (
    <div className="mt-6 flex items-center justify-center gap-3 text-sm">
      <button className="btn-secondary" disabled={page === 0} onClick={() => onChange(page - 1)}>
        Précédent
      </button>
      <span>
        Page {page + 1} / {totalPages}
      </span>
      <button className="btn-secondary" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>
        Suivant
      </button>
    </div>
  );
}
