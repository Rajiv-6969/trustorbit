/** The TrustOrbit crew – shown on the Crew page, the footer and every PDF report. */
export interface CrewMember {
  name: string;
  roll: string;
  role: string;
  module: string;
}

export const CREW: CrewMember[] = [
  {
    name: "Akash",
    roll: "1441",
    role: "Team lead & integration",
    module: "Project setup, integration, CI, final testing",
  },
  {
    name: "Abhi Rathod",
    roll: "1436",
    role: "Data ingestion",
    module: "M1: NASA/ISRO dataset selection, CSV/JSON/API import",
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
    module: "M4: UI, PostGIS storage, map view, PDF export",
  },
];
