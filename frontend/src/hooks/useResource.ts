import { useEffect, useState } from "react";
import { ApiError } from "../api/client";
export function useResource<T>(loader: () => Promise<T>, version = 0) {
  const [data, setData] = useState<T>();
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [errorStatus, setErrorStatus] = useState<number>();
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    setErrorStatus(undefined);
    setData(undefined);
    loader()
      .then((value) => {
        if (active) setData(value);
      })
      .catch((error: unknown) => {
        if (active) {
          setError(
            error instanceof Error
              ? error.message
              : "Unable to load this entry. Please try again.",
          );
          setErrorStatus(error instanceof ApiError ? error.status : undefined);
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [loader, version, retry]);
  return {
    data,
    error,
    errorStatus,
    loading,
    reload: () => setRetry((value) => value + 1),
  };
}
