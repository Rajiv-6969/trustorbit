# data/isro – converted ISRO station time series

Created by `IsroFileReader` (run via the `download` or `convert` command). One CSV per
product and satellite, e.g. `lst_insat-3ds.csv`, `hem_insat-3dr.csv`.

| column | meaning |
|---|---|
| station_id | code from `data/stations.csv` |
| source | `MOSDAC` |
| satellite | `INSAT-3DR` or `INSAT-3DS` |
| product | `LST` (land surface temperature) or `HEM` (rainfall, Hydro-Estimator) |
| timestamp_utc | acquisition time from the file name (UTC) |
| lat, lon | centre of the satellite pixel used (nearest to the station) |
| value | reading in `units`; **empty when missing** |
| units | as given in the file (e.g. `K`) – converted later by M2 |
| quality_flag | the product's own quality flag, if it has one |
| status | `OK`, `FILL` (fill value / no retrieval), `INVALID_QC`, `NO_PIXEL` |
| file_name | MOSDAC file the value came from |
| read_at_utc | when TrustOrbit read the file |

These are value-added extracts (10 pixels per file plus our quality status), not the original
MOSDAC products. Data Source MOSDAC/SAC/ISRO. https://mosdac.gov.in
