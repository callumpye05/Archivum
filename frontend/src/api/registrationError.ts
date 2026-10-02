import { ApiError } from "./client";

// Use safe copy rather than echoing a server response that may contain input.
export function registrationError(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return "Could not reach Archivum. Check your connection and try again.";
  }
  if (error.status === 400 || error.status === 409) {
    const duplicate = /already|duplicate|taken|exists|in use|conflict/i.test(
      error.message,
    );
    if (duplicate || error.status === 409) {
      const email = /email|e-mail/i.test(error.message);
      const username = /user[\s_-]?name/i.test(error.message);
      if (email && !username)
        return "This email is already registered. Use another email.";
      if (username && !email)
        return "This username is already taken. Choose another username.";
      return "This username or email is already registered. Choose another username or email.";
    }
    return "Your details were not accepted. Check your username, email, and password, then try again.";
  }
  return "Registration could not be completed. Please try again shortly.";
}
