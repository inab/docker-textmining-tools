UMLS-TAGGER
========================

<b>Tagger of UMLS terminology</b>   

========================

UMLS integrates and distributes key terminology, classification and coding standards, and associated resources to promote creation of more effective and interoperable biomedical information systems and services, including electronic health records. 

The Metathesaurus, which contains over one million biomedical concepts from over 100 source vocabularies is used by the UMLS-TAGGER.

The UMLS-TAGGER annotate documents with the UMLS Metathesaurus terminology, given a configuration file indicating the sources and the semantic types to be used.  

Takes the Metathesaurus Data Files, the RRF files (Rich Release Format) generated after the execution of MetamorphoSys witch is the installation and customization tool of UMLS.
MetamorphoSys comes together with the UMLS download.

To overview UMLS please go to: 
https://www.nlm.nih.gov/research/umls/new_users/online_learning/OVR_001.html 

To install UMLS with MetamorphoSys please go to: 
https://www.nlm.nih.gov/research/umls/implementation_resources/metamorphosys/help.html

The output of the tagger will be GATE files with the given annotated terminology. 

========================

<b>Requirements:</b>

The installation of UMLS throught MetamorphoSys.   

With the execution and installation of MetamorphoSys a subset of terminologies will be generated given your own configuration.  For more information go to https://www.nlm.nih.gov/research/umls/implementation_resources/metamorphosys/help.html

The installation directory has to be provided for the execution of the tagger, has to be the META directory that contains the RRF files of Metathesaurus.

========================

<b>Configuration:</b>

A configuration file has to be provided, if not a default one will be used, that contains information regarding with the sources and the semantic type that will be used during the tagging process.
Here an example:

[SOURCES]
#all sources present in the umls subset, no spaces between pipes
sources=ALL_SOURCES
#specific sources, this are the umls sources codes
#sources=MDR|SNOMEDCT_VET|
[SEMANTIC_TYPES]
#This describes the mapping between the UMLS classification of terms and the Labels that the user want to obtain.
#Each line is a mapping; separated by |.
#The first element is the UMLS semantic type, the second is only a description of the semantic type from umls; and the 
#third one is the LABEL that we are going to obtain if a term is reached.
#SPECIES
T011|Amphibian|SPECIES
T010|Vertebrate|SPECIES
#ANATOMY
T018|Embryonic Structure|ANATOMY
[SEMANTIC_TYPES_END]
#This are the excluded semantic types by source.
[EXCLUDED_SEMANTIC_TYPES_BY_SOURCE]
SNOMEDCT_US=T033
SNOMEDCT_VET=T033
[EXCLUDED_SEMANTIC_TYPES_BY_SOURCE_END]

In this example all the sources of the MetamorphoSys subset will be used; and only the semantic types:  Amphibian, Vertebrate and Embryonic Structure will be annotated.  Each of these semantic types will 
be mapping and annotated with the corresponding label.  And if you consider that a specific source is generating a lot of noise in your tagging task, you can excluded in the [EXCLUDED_SEMANTIC_TYPES_BY_SOURCE].
In that example the semanticTypes T033 from the source SNOMEDCT_US and SNOMEDCT_VET is excluded.

One of the important tasks is to analyze and define witch sources and semantic types are important to your analysis;  

[SOURCES], [SEMANTIC_TYPES] and [SEMANTIC_TYPES_END] are required and have to be present in the file.

### For clone this component

	git clone --depth 1 https://github.com/inab/docker-textmining-tools.git umls-tagger
	cd umls-tagger
	git filter-branch --prune-empty --subdirectory-filter umls-tagger HEAD

### Build and Run the Docker 

	# To build the docker, just go into the umls-tagger folder and execute
	docker build -t umls-tagger .
	#To run the docker, just set the input_folder and the output
	mkdir ${PWD}/umls_output; docker run --rm -u $UID -v /home/user/2018AB/EXAMPLE/META:/in_umls:ro -v ${PWD}/input_output:/in:ro -v ${PWD}/umls_output:/out:rw umls-tagger umls-tagger -u /in_umls -c /in/config.properties -i /in -o /out -d /out
		
Parameters:
<p>
-u input directory of the UMLS subset where the RRF files are located,  usually are in ... META folder
</p>
<p>
-c configuration file that contains the semantic type mappings and the sources to be used during the mapping.  If no configuration file is provided, a default one will be used.
</p>
<p>
-i input folder with the documents to annotated. The documents could be plain txt or xml GATE-formated documents.
</p>
<p>
-o output folder with the documents annotated in gate format.
</p>
<p>
-a Annotation set where the annotation will be included.
</p>
<p>
-d Optional destination folder of internal dictionary generated from the umls terminology, if not an internal path is used. This option is recommended if you want to have access to the gazetter generated with your configuration. 
</p>


		
		
