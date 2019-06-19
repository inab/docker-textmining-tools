gnormplus-gate-wrapper
========================

<b>This component is a Gate Wrapper of the GNormPlus application. Could be easily download and the main objective is to run as a docker container inside a batch pipeline.
</b>   

========================

GNormPlus: An Integrative Approach for Tagging Gene, Gene Family and Protein Domain

GNormPlus is an end-to-end system that handles both gene/protein name and identifier detection in biomedical literature, including gene/protein mentions, family names and domain names. Moreover, GNormPlus also integrates several advanced text-mining techniques (i.e., GenNorm, SR4GN, SimConcept, Ab3P and CRF++) for resolving composite gene names. On two public benchmarking datasets, we show that GNormPlus compares favorably to the other state-of-the-art methods. 

For more detailed information:
https://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/tmTools/GNormPlus.html

<ul>
<li>
Wei C-H, Kao H-Y, Lu Z (2015) "GNormPlus: An Integrative Approach for Tagging Gene, Gene Family and Protein Domain", BioMed Research International Journal, Text Mining for Translational Bioinformatics special issue, BioMed Research International Journal, Article ID 918710; DOI: dx.doi.org/10.1155/2015/918710
</li>
</ul>

========================

Build and run the docker individually
	
	# To build the docker, just go into the gnormplus-gate-wrapper folder and execute
	docker build -t gnormplus-gate-wrapper .
	#To run the docker, just set the input_folder and the output
	mkdir ${PWD}/gnormplus_output; docker run --rm -u $UID -v ${PWD}/input_folder:/in:ro -v ${PWD}/gnormplus_output:/out:rw gnormplus-gate-wrapper gnormplus-gate-wrapper -i /in -o /out -a MY_SET
	
		
Parameters:
<p>
-i input folder with the documents to annotated. The documents could be plain txt or xml gate documents.
</p>
<p>
-o output folder with the documents annotated in gate xml format.
</p>
<p>
-a annotation set output. 
</p>

<p>If you want, just replace "input_folder" for your directory that contains the files, and execute. You can also replace the name of the output folder "dnorm_output"</p>
		
		
