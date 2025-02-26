import os
import zipfile
import tarfile
from tqdm import tqdm  # For displaying progress bars


def extract_archive(file_path, output_dir):
    """
    Extract a single archive file.
    :param file_path: Path to the archive file
    :param output_dir: Directory to extract the files to
    """
    try:
        # Determine the compression format based on the file extension
        if file_path.endswith(".zip"):
            with zipfile.ZipFile(file_path, 'r') as zip_ref:
                for file in tqdm(zip_ref.namelist(), desc=f"Extracting {os.path.basename(file_path)}", unit="file"):
                    zip_ref.extract(file, output_dir)
        elif file_path.endswith((".tar", ".tar.gz", ".tgz", ".tar.xz", ".gz")):
            with tarfile.open(file_path, 'r') as tar_ref:
                members = tar_ref.getmembers()
                for member in tqdm(members, desc=f"Extracting {os.path.basename(file_path)}", unit="file"):
                    tar_ref.extract(member, output_dir)
        else:
            print(f"Unsupported file format: {file_path}")
            return False
        print(f"✅ Successfully extracted: {file_path} to {output_dir}")
        return True
    except Exception as e:
        print(f"❌ Extraction failed: {file_path} Error message: {e}")
        return False


def extract_all(source_dir, target_dir):
    """
    Extract all archive files in a directory.
    :param source_dir: Directory containing the archive files
    :param target_dir: Directory to extract the files to
    """
    if not os.path.exists(target_dir):
        os.makedirs(target_dir)

    # Get all files in the source_dir
    files = [os.path.join(root, file)
             for root, _, filenames in os.walk(source_dir) for file in filenames]

    print(f"Found {len(files)} files, starting extraction...\n")

    for file_path in tqdm(files, desc="Overall progress", unit="file"):
        # Create a separate output folder for each archive
        output_subdir = os.path.join(target_dir, os.path.splitext(os.path.basename(file_path))[0])
        if not os.path.exists(output_subdir):
            os.makedirs(output_subdir)

        # Extract the file
        success = extract_archive(file_path, output_subdir)
        if not success:
            # Log failed files
            with open(os.path.join(target_dir, "error_log.txt"), "a") as log_file:
                log_file.write(f"Extraction failed: {file_path}\n")


if __name__ == "__main__":
    from argparse import ArgumentParser

    # Add command-line argument support
    parser = ArgumentParser(description="Extract all archive files in the sourceCode folder and display progress")
    parser.add_argument("--source", type=str, default="./sourceCode", help="Directory containing the archive files (default: ./sourceCode)")
    parser.add_argument("--target", type=str, default="./extracted", help="Output directory for extracted files (default: ./extracted)")
    args = parser.parse_args()

    # Print source and target directories
    print(f"Source directory: {args.source}")
    print(f"Target directory: {args.target}")

    # Call the extraction function
    extract_all(args.source, args.target)