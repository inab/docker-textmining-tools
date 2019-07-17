docker build -t nlp-standard-preprocessing:1.0 nlp-standard-preprocessing
docker build -t linnaeus-gate-wrapper:1.0 linnaeus-gate-wrapper
docker build -t dnorm-gate-wrapper:1.0 dnorm-gate-wrapper
docker build -t umls-tagger:1.0 umls-tagger
docker build -t hepatotoxicity-annotation:1.0 dictionaries-annotation/hepatotoxicity-annotation
docker build -t cdisc-etox-annotation:1.0 dictionaries-annotation/cdisc-etox-annotation
docker build -t ades-tagger:1.0 ades_tagger
docker build -t ades-ner-postprocessing:1.0 ades-ner-postprocessing
docker build -t ades-relation-extraction:1.0 ades-relation-extraction
docker build -t ades-export-to-json:1.0 ades-export-to-json
docker build -t import-json-to-mongo:1.0 import-json-to-mongo

#docker pull mongo:4.0.4
#docker run -d -p 27017-27019:27017-27019 -v ~/mongo-data:/data/db --name mongodb mongo:4.0.4
