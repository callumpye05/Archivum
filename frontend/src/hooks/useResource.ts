import { useEffect, useState } from "react";
export function useResource<T>(loader: () => Promise<T>, version = 0) {
  const [data, setData] = useState<T>();
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    setData(undefined);
    loader()
      .then((value) => {
        if (active) setData(value);
      })
      .catch((error: Error) => {
        if (active) setError(error.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [loader, version, retry]);
  return { data, error, loading, reload: () => setRetry((value) => value + 1) };
}
