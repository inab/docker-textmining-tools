# Rough instructions to build the docker images

#docker build -t tesseract-poppler tesseract-poppler
#docker build -t grobid grobid
#docker build -t cermine cermine
#docker build -t ocrmypdf ocrmypdf

docker build -t nlp-standard-preprocessing:1.0:1.0 nlp-standard-preprocessing:1.0
docker build -t linnaeus-gate-wrapper:1.0 linnaeus-gate-wrapper
docker build -t dnorm-gate-wrapper:1.0 dnorm-gate-wrapper
docker build -t umls-tagger:1.0 umls-tagger
docker build -t hepatotoxicity-annotation:1.0 dictionaries-annotation/hepatotoxicity-annotation
docker build -t ades-tagger:1.0 ades_tagger
docker build -t ades-ner-postprocessing:1.0 ades-ner-postprocessing
docker build -t ades-relation-extraction:1.0:1.0 ades-relation-extraction

#To run the pipeline (ocrmypdf + grobid) with nextflow
#./nextflow run pipeline.nf
