# SSFuzz

## 1. Environmental Requirements

### 2.1 Basic Environment Deployment

```
openjdk 11.0.23
Python 3.11.5
```


For a smooth setup, it is recommended to use the provided Dockerfile and docker-compose.yml to deploy the basic environment.

### 2.2 Fuzzing Configuration

```bash
conda activate ssfuzz
```

- In the `00JVMs` directory:
  - Use `step01_developmentJVMs.py` to deploy the JVM for differential testing.
  - Use `step02_checkVersion.py` to verify the installation.
  - Use `step03_testHarness.py` to ensure differential testing is functioning correctly.

### 2.3 Access the Working Directory

```bash
cd /root/ssfuzz/  ## [path]/ssfuzz
git pull
```

### 2.4 Database Initialization

```bash
cd /root/ssfuzz/workline/web/
python manage.py loaddata analysis/fixtures/testbed.json
```

## 2. Generate Seeds

- **Defect-triggering code extraction**: `02segmentLocate/PRGetter`
- **Training code**: `01seedGenerate` folder

Use the `step0_generate_seed.py` script to generate seeds:

```bash
cd /root/ssfuzz/workline/
python /root/ssfuzz/workline/step0_generate_seed.py --num_return_sequences=100 --total_iterations=2
```

> **Note**: To ensure successful reproduction, ensure your host machine has an *Nvidia GPU* with the appropriate drivers installed. This helps keep generation time within a reasonable range. (CPU generation is also supported and is automatically detected without additional configuration.)

## 3. Synthesizing Test Cases

**Recommended**: Run directly from Maven

```bash
cd /root/ssfuzz/parser/feature
mvn clean install -DskipTests
java -jar /root/ssfuzz/parser/feature/target/InsertMain-1.0-shaded.jar org.example.InsertMain
```

- The `step101_llm.py` script outlines the logic for synthesizing test cases using large models.
- The `step102_AItestcase.py` script extracts the generated test cases from logs and prepares them for differential testing.

## 4. Differential Fuzzing

```bash
cd /root/ssfuzz/workline
python /root/ssfuzz/workline/step3_harness.py
```
