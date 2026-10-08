import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import App from "./App";

describe("Launch pad placeholder", () => {
  it("shows the name, tagline and all five crew members", () => {
    render(<App />);
    expect(screen.getByRole("heading", { name: "TrustOrbit" })).toBeInTheDocument();
    expect(screen.getByText("Not all satellite data is created equal.")).toBeInTheDocument();
    for (const name of [
      "Akash",
      "Abhi Rathod",
      "Sameer Basha",
      "Rajiv Siddharth",
      "Saikirantejas GS",
    ]) {
      expect(screen.getByText(new RegExp(name))).toBeInTheDocument();
    }
  });
});
