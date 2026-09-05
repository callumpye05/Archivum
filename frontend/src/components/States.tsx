export function ErrorState({
  message,
  retry,
}: {
  message: string;
  retry?: () => void;
}) {
  return (
    <div className="state error" role="alert">
      <h3>Unable to complete this request</h3>
      <p>{message}</p>
      {retry && <button onClick={retry}>Try again</button>}
    </div>
  );
}
export function Loading() {
  return (
    <div className="state" role="status">
      <span className="spinner" />
      Opening the archive…
    </div>
  );
}
export function Empty({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <div className="state empty">
      <span className="empty-mark" aria-hidden="true">
        ✧
      </span>
      <h3>{title}</h3>
      <p>{children}</p>
    </div>
  );
}
export function dateLabel(date: string | null) {
  return date
    ? new Date(date).toLocaleDateString(undefined, {
        month: "short",
        day: "numeric",
        year: "numeric",
      })
    : "Date unavailable";
}
