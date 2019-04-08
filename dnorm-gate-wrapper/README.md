Dnorm-Gate-Wrapper
========================

<b>This component is a Gate wrapper of the Dnorm application.  Could be easily downloaded and run as a docker container.
</b>   

========================

Dnorm: Disease name normalization with pairwise learning to rank.

DNorm is an automated method for determining which diseases are mentioned in biomedical text, the task of disease normalization. 

The machine learning approach for DNorm uses the NCBI disease corpus and the MEDIC vocabulary, which combines MeSH and OMIM.
Disease mentions are then located using the BANNER named entity recognizer. BANNER is a trainable system, using conditional random fields and a rich feature set approach.
DNorm algorithm achieves 0.782 micro-averaged F-measure and 0.809 macro-averaged F-measure.

For more detailed information:
https://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/tmTools/DNorm.html

<ul>
<li>
Robert Leaman, Rezarta Islamaj Doǧan and Zhiyong Lu, DNorm: Disease Name Normalization with Pairwise Learning to Rank. Bioinformatics (2013) 29 (22): 2909-2917, doi:10.1093/bioinformatics/btt474
</li>
<li>
Robert Leaman, Ritu Khare and Zhiyong Lu, NCBI at 2013 ShARe/CLEF eHealth Share Task: Disorder Normalization in Clinical Notes with DNorm. Working Notes of the Conference and Labs of the Evaluation Forum (2013)
</li>
<li>
Robert Leaman and Zhiyong Lu, Automated Disease Normalization with Low Rank Approximations. Proceedings of BioNLP 2014: pp 24-28
</li>
</ul>

========================

Build and run the docker individually
	
	# To build the docker, just go into the linnaeus-gate-wrapper folder and execute
	docker build -t dnorm-gate-wrapper .
	#To run the docker, just set the input_folder and the output
	mkdir ${PWD}/dnorm_output; docker run --rm -u $UID -v ${PWD}/input_folder:/in:ro -v ${PWD}/dnorm_output:/out:rw dnorm-gate-wrapper dnorm-gate-wrapper -i /in -o /out
	
		
To parameters:
<p>
-i input folder with the documents to annotated. The documents could be plain txt or xml gate documents.
</p>
<p>
-o output folder with the documents annotated in gate xml format.
</p>

		
		
