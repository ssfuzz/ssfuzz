import os
from transformers import AutoModel, AutoTokenizer

def download_and_save_model(model_name, save_dir):
    """
    Download a model from Hugging Face and save it to a specified relative directory.

    Args:
        model_name (str): Name of the Hugging Face model (e.g., 'bert-base-uncased').
        save_dir (str): Relative path to save the model.
    """
    # Create the save directory if it doesn't exist
    save_dir = os.path.normpath(save_dir)  # Normalize the path for cross-platform compatibility
    os.makedirs(save_dir, exist_ok=True)

    # Download the model and tokenizer
    print(f"Downloading model '{model_name}'...")
    model = AutoModel.from_pretrained(model_name)
    tokenizer = AutoTokenizer.from_pretrained(model_name)

    # Save the model and tokenizer locally
    print(f"Saving model and tokenizer to '{save_dir}'...")
    model.save_pretrained(save_dir)
    tokenizer.save_pretrained(save_dir)
    print(f"Model '{model_name}' successfully saved to '{save_dir}'.")

if __name__ == "__main__":
    # Example usage
    model_name = "distilbert/distilgpt2"  # Replace with the desired model name
    save_dir = "models/distilbert/distilgpt2"  # Relative path to save the model

    download_and_save_model(model_name, save_dir)
