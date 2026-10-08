import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import App from "./App";

describe("Home placeholder", () => {
  it("shows the name, the MOSDAC credit and all five team members", () => {
    render(<App />);
    expect(screen.getByRole("heading", { name: "TrustOrbit" })).toBeInTheDocument();
    expect(screen.getByText(/Data Source MOSDAC\/SAC\/ISRO/)).toBeInTheDocument();
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
