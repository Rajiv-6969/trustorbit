/** The TrustOrbit team – shown on the Team page, the footer and every PDF report. */
export interface TeamMember {
  name: string;
  roll: string;
  role: string;
  module: string;
}

export const TEAM: TeamMember[] = [
  {
    name: "Akash",
    roll: "1441",
    role: "Team lead & integration",
    module: "Project setup, Main pipeline, CI, final testing",
  },
  {
    name: "Abhi Rathod",
    roll: "1436",
    role: "Data ingestion",
    module: "M1: ISRO dataset selection, file reading/conversion",
  },
  {
    name: "Sameer Basha",
    roll: "1446",
    role: "Data preprocessing",
    module: "M2: cleaning, normalisation, missing-value handling",
  },
  {
    name: "Rajiv Siddharth",
    roll: "1420",
    role: "AI / ML engine",
    module: "M3: Weka anomaly detection, 0–100 reliability score",
  },
  {
    name: "Saikirantejas GS",
    roll: "1464",
    role: "Dashboard & reports",
    module: "M4: data store, JSON export, PDF reports, website",
  },
];
