---
library_name: transformers
base_model: ./models/distilbert/distilgpt2
tags:
- generated_from_trainer
model-index:
- name: finetuneResults
  results: []
---

<!-- This model card has been generated automatically according to the information the Trainer had access to. You
should probably proofread and complete it, then remove this comment. -->

# finetuneResults

This model is a fine-tuned version of [./models/distilbert/distilgpt2](https://huggingface.co/./models/distilbert/distilgpt2) on an unknown dataset.

## Model description

More information needed

## Intended uses & limitations

More information needed

## Training and evaluation data

More information needed

## Training procedure

### Training hyperparameters

The following hyperparameters were used during training:
- learning_rate: 5e-05
- train_batch_size: 4
- eval_batch_size: 8
- seed: 42
- optimizer: Adam with betas=(0.9,0.999) and epsilon=1e-06
- lr_scheduler_type: linear
- lr_scheduler_warmup_steps: 500
- num_epochs: 100

### Training results



### Framework versions

- Transformers 4.44.2
- Pytorch 2.3.1+cu121
- Datasets 2.19.1
- Tokenizers 0.19.1
