import { useCallback, useEffect, useRef, useState } from "react";
import { updateUser } from "../api";
import {
  ApiError,
  getCurrentUser,
  onUnauthorized,
  signIn,
  signOut,
} from "../api/client";
import type { UpdateUserDto } from "../types";

export function useAuth() {
  const [status, setStatus] = useState<
    "loading" | "error" | "guest" | "authenticated"
  >("loading");
  const [username, setUsername] = useState<string | null>(null);
  const [signedOut, setSignedOut] = useState(false);
  const revision = useRef(0);
  const checkSession = useCallback(async () => {
    const current = ++revision.current;
    setStatus("loading");
    try {
      const user = await getCurrentUser();
      if (current !== revision.current) return;
      setUsername(user.userName);
      setStatus("authenticated");
    } catch (error) {
      if (current !== revision.current) return;
      setStatus(
        error instanceof ApiError && error.status === 401 ? "guest" : "error",
      );
    }
  }, []);
  useEffect(() => {
    const unsubscribe = onUnauthorized(() => {
      ++revision.current;
      setUsername(null);
      setStatus("guest");
    });
    void checkSession();
    return () => {
      ++revision.current;
      unsubscribe();
    };
  }, [checkSession]);
  const login = useCallback(async (username: string, password: string) => {
    const current = ++revision.current;
    const user = await signIn(username, password);
    if (current !== revision.current) return;
    setSignedOut(false);
    setUsername(user.userName);
    setStatus("authenticated");
  }, []);
  const logout = useCallback(async () => {
    await signOut();
    ++revision.current;
    setSignedOut(true);
    setUsername(null);
    setStatus("guest");
  }, []);
  const saveAccount = useCallback(
    async (data: UpdateUserDto) => {
      const current = revision.current;
      const updatedUsername = await updateUser(data);
      if (current !== revision.current) return;
      // The backend still looks up the old principal after a username change.
      // End that session and let the user sign in with their new details.
      if (updatedUsername !== username) await logout();
      else setUsername(updatedUsername);
    },
    [username, logout],
  );
  return {
    status,
    login,
    logout,
    username,
    signedOut,
    saveAccount,
    checkSession,
  };
}
