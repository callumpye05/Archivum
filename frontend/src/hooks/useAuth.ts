import { useCallback, useEffect, useRef, useState } from "react";
import { updateUser } from "../api";
import { clearAuthorization, onUnauthorized, signIn } from "../api/client";
import type { UpdateUserDto } from "../types";

export function useAuth() {
  const [status, setStatus] = useState<"guest" | "authenticated">("guest");
  const [username, setUsername] = useState<string | null>(null);
  const [signedOut, setSignedOut] = useState(false);
  const revision = useRef(0);
  useEffect(() => {
    // Every mount starts a new, explicit-login-only frontend session.
    clearAuthorization();
    const unsubscribe = onUnauthorized(() => {
      ++revision.current;
      setUsername(null);
      setStatus("guest");
    });
    return () => {
      ++revision.current;
      unsubscribe();
      clearAuthorization();
    };
  }, []);
  const login = useCallback(async (username: string, password: string) => {
    const current = ++revision.current;
    await signIn(username, password);
    if (current !== revision.current) return;
    setSignedOut(false);
    setUsername(username);
    setStatus("authenticated");
  }, []);
  const saveAccount = useCallback(async (data: UpdateUserDto) => {
    const current = revision.current;
    const updatedUsername = await updateUser(data);
    if (current === revision.current) setUsername(updatedUsername);
  }, []);
  const logout = useCallback(() => {
    ++revision.current;
    clearAuthorization();
    setSignedOut(true);
    setUsername(null);
    setStatus("guest");
  }, []);
  return { status, login, logout, username, signedOut, saveAccount };
}
