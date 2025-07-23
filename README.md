```markdown
# Car Processor

A Java 17 command-line application for parsing and processing car data from XML and CSV files.

## Installation

1. **Prerequisites**:
   - Java 17 JDK
   - Gradle Wrapper (included, uses Gradle 8.10)

2. **Setup Gradle Wrapper** (if not already present):
   ```bash
   gradle wrapper --gradle-version 8.10
   ```

3. **Build**:
   ```bash
   ./gradlew clean build
   ```

4. **Run**:
   ```bash
   java -jar build/libs/car-processor-1.0-SNAPSHOT.jar
   ```

## Usage

The application automatically loads `cars.xml` and `brands.csv` from `src/main/resources/` without displaying loading messages. Commands `filter` and `sort` automatically display results in a table format unless the output format is changed with the `output` command. The car list resets to its initial state after each `filter` or `sort` command. Available commands:

- `filter brand-price <brand|null> <min-price> <max-price> <currency>`: Filter by brand and price, outputs table.
- `filter brand-date <brand|null> <start-date> <end-date>`: Filter by brand and release date (MM/dd/yyyy, e.g., 01/15/2023), outputs table.
- `sort year`: Sort by release year (latest to oldest), outputs table.
- `sort price <currency>`: Sort by price (highest to lowest), outputs table.
- `sort currency-type`: Sort SUVs (EUR), Sedans (JPY), Trucks (USD), outputs table.
- `output <table|xml|json>`: Set output format for subsequent filter/sort commands (default: table).
- `help`: Show help.
- `exit`: Exit.

## Resource Files

- **`cars.xml`**: Must follow the structure:
  ```xml
  <cars>
      <car>
          <type>SUV</type>
          <model>RAV4</model>
          <price currency="USD">25000.00</price>
          <prices>
              <price currency="EUR">23000.00</price>
              <price currency="GBP">20000.00</price>
              <price currency="JPY">2800000.00</price>
          </prices>
      </car>
      <!-- More cars -->
  </cars>
  ```
- **`brands.csv`**: Must follow the structure:
  ```csv
  Brand,ReleaseDate
  Toyota,01/15/2023
  Nissan,08/22/2023
  <!-- More brands -->
  ```
  - Each row must have a `brand` (non-empty string) and `releaseDate` (MM/dd/yyyy, e.g., 01/15/2023).
  - No empty lines, trailing commas, or missing values.
  - Use commas (`,`) as delimiters and Unix-style line endings (LF).

## Example

```bash
car-processor> filter brand-price Toyota 20000 40000 USD
Filtered to 1 cars.
Type       Model           Brand      USD        EUR        GBP        JPY        Release Date
--------------------------------------------------------------------------------
SUV        RAV4           Toyota     25000.00   23000.00   20000.00   2800000.00 2023-01-15
car-processor> filter brand-date Toyota 01/15/2023 01/15/2023
Filtered to 1 cars.
Type       Model           Brand      USD        EUR        GBP        JPY        Release Date
--------------------------------------------------------------------------------
SUV        RAV4           Toyota     25000.00   23000.00   20000.00   2800000.00 2023-01-15
car-processor> output json
[
  {
    "type": "SUV",
    "model": "RAV4",
    "brand": "Toyota",
    "prices": {
      "USD": 25000.0,
      "EUR": 23000.0,
      "GBP": 20000.0,
      "JPY": 2800000.0
    },
    "releaseDate": "2023-01-15"
  },
  ...
]
car-processor> filter brand-price Nissan 20000 40000 USD
Filtered to 1 cars.
Type       Model           Brand      USD        EUR        GBP        JPY        Release Date
--------------------------------------------------------------------------------
SUV        Rogue          Nissan     30000.00   28000.00   25000.00   3300000.00 2023-08-22
car-processor> exit
```

## Error Handling

- Missing resource files: "Failed to load cars.xml or brands.csv: ..."
- Invalid CSV rows: Skips rows with missing or invalid data, logs warnings (e.g., "Skipping invalid row at line X: ...").
- Invalid date formats: Skips rows with invalid dates (must be MM/dd/yyyy) for `brands.csv`, or throws error for CLI commands (expected MM/dd/yyyy).
- Unknown commands: "Unknown command: ..."

## Extensibility

- Add new filters by implementing `CarFilter`.
- Add new sorters by implementing `CarSorter`.
- Add new output formats by implementing `OutputFormatter`.
```