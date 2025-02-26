# Environment Initialization


## Conda Initialization
Create the conda environment directory, you need to download conda in advance.

## step1: make conda environment directory
```bash
cd ~/anaconda/env
mkdir ssfuzz ##[your_conda_name]
```

## step2: Unzip the environment zip

```bash
cd ~/anaconda/env/ssfuzz
sudo tar -xjf ssfuzz-0.0-py31_0.tar.bz2 ##[your_conda_name]
```

OR `conda env create -f ssfuzz.yaml -n ssfuzz`

> ssfuzz-0.0-py31_0.tar.bz2: https://zenodo.org/records/13340493



## step3: Activate ssfuzz

```
conda env list
conda activate ssfuzz
```

## Database Initialization

```bash
cd /root/ssfuzz/workline/web

python manage.py makemigrations analysis

python manage.py migrate analysis

python manage.py loaddata analysis/fixtures/testbed.json

```
