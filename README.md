# charaparser-web

## setup / how to run
1. Configure as Maven project
2. Make sure all the charaparser [dependencies](https://github.com/biosemantics/charaparser) are met
3. Start the container with services by running the [Application](https://github.com/biosemantics/charaparser-web/blob/master/src/main/java/edu/arizona/biosemantics/semanticmarkup/web/Application.java)

## service endpoints
* /parse: *Parses morphological descriptive text*
  * Single sentence
    * HTTP GET http://{host}/parse?sentence={URL_encoded_sentence}
    * {URL_encoded_sentence}: The sentence to be parsed
    * Example: GET http://shark.sbs.arizona.edu:8080/parse?sentence=leaf-blade%20orbicular,%206%E2%80%9310%20%C3%97%206%E2%80%9310%20cm
  * Multi sentence
    * HTTP GET http://{host}/parse?description={URL_encoded_description}
    * {URL_encoded_description}: The description to be parsed. A description can contain of multiple sentences.
    * Example: GET http://shark.sbs.arizona.edu:8080/parse?description=Herbs%2C%20perennial%2C%20cespitose%20or%20not%2C%20rhizomatous%2C%20rarely%20stoloniferous.%20Culms%20usually%20trigonous%2C%20sometimes%20round.%20Leaves%20basal%20and%20cauline%2C%20sometimes%20all%20basal%3B
  * The service will respond with a JSON body based on charaparser's [XML output schema](https://github.com/biosemantics/schemas/blob/master/semanticMarkupOutput.xsd). An example follows.
    ```json
    {
      "statements": [
        {
          "id": "d1_s0",
          "text": "leaf-blade orbicular, 6–10 × 6–10 cm",
          "biologicalEntities": [
            {
              "characters": [
                {
                  "isModifier": "false",
                  "name": "shape",
                  "ontologyId": "http://purl.obolibrary.org/obo/PATO_0001934[orbicular:circular/orbicular:1.0]",
                  "src": "d1_s0",
                  "value": "orbicular"
                },
                {
                  "charType": "range_value",
                  "name": "length",
                  "src": "d1_s0",
                  "value": "6 cm - 10 cm"
                },
                {
                  "charType": "range_value",
                  "name": "width",
                  "src": "d1_s0",
                  "value": "6 cm - 10 cm"
                }
              ],
              "id": "o154",
              "name": "leaf-blade",
              "nameOriginal": "leaf-blade",
              "src": "d1_s0",
              "type": "structure"
            }
          ],
          "relations": []
        }
      ]
    }
    ```
* /createUserOntology: *Make a (set) of ontology ready. This service needs to be called before any of the requests listed below can be used*
  * HTTP POST http://{host}/createUserOntology
  * Request body: user can take an empty string as its value, in this case, a shared ontology will be made ready for all requests with an empty user. If user has a non-empty value, such as an id, a copy of the ontology will be made ready for this specific user. Subsequent calls to access the ontology will need to use user field with the id. Ontologies can be exp or carex.
  ```json
  {
	  "user":"2",
	   "ontologies":"exp"
  }
  ```
  
* /{ontology}/search: *Searches an ontology for a term*
  * HTTP GET http://{host}/{ontology}/search?user={optional_user}&term={term}&ancestorIRI={ancestorIRI}&parent={optional_parent}&relation={optional_relation}
  * {ontology}: The ontology to search for the {term}. Ontology must be in lower case, e.g., exp.
  * {optional_user}: If present, the user specific version of the ontology will be used for the search. Otherwise, a shared version of the ontology will be used (See /createUserOntology).
  * {term}: The term to search for
  * {optional_ancestorIRI}: The ancestor the search term must have. Use %23 for # in the IRI.
  * {optional_parent}: The optional parent the {term} is required to have
  * {optional_relation}: The optional relation the {term} is required to have
  * Example: 
  GET http://shark.sbs.arizona.edu:8080/carex/search?term=reddish&ancestorIRI=http://biosemantics.arizona.edu/ontologies/carex%23colored
  GET http://shark.sbs.arizona.edu:8080/carex/search?term=quaternary%20leaf%20vein (this works only after a call to /createUserOntology with an empty user and carex ontology as parameters)
  * Response body: returns classes related to the term in someway, such as a synonym, or other relationships.  
    ```json
    {
      "entries": [
        {
          "score": 1,
          "term": "quaternary leaf vein",
          "parentTerm": "leaf lamina vein",
          "resultAnnotations": [
            {
              "property": "elucidation",
              "value": "http://googledrive.com/image.jpg"
            },
            {
              "property": "part of",
              "value": "PO:0025034"
            },
            
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasDbXref",
              "value": "FNA:dba43715-e71f-4192-87a2-489f5b9b4c82"
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasDbXref",
              "value": "Gramene:Chih-Wei_Tung"
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#id",
              "value": "PO:0008022"
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasDbXref",
              "value": "PO_GIT:435"
            },
            {
              "property": "http://purl.obolibrary.org/obo/IAO_0000115",
              "value": "A leaf lamina vein (PO:0020138) arising from a tertiary leaf vein (PO:0008021)."
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasSynonymType",
              "value": ""
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasDbXref",
              "value": "NIG:Yukiko_Yamazaki"
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasExactSynonym",
              "value": "vena cuaternaria (Spanish, exact)"
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasSynonymType",
              "value": ""
            },
            {
              "property": "http://www.w3.org/2000/01/rdf-schema#label",
              "value": "quaternary leaf vein"
            },
           {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasDbXref",
              "value": "FNA:e3ab3fff-3015-4b76-af51-fc69ee9396d8"
            },
            {
              "property": "http://www.w3.org/2000/01/rdf-schema#comment",
              "value": "Vein orders only apply to hierarchically branching vein patterns, not to dichotomously branching vein patterns, as found in some ferns and gymnosperms."
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasNarrowSynonym",
              "value": "veinlet (narrow)"
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasDbXref",
              "value": "POC:Maria_Alejandra_Gandolfo"
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasOBONamespace",
              "value": "plant_anatomy"
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasNarrowSynonym",
              "value": "cross-vein (narrow)"
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym",
              "value": "fourth order leaf vein (related)"
            },
            {
              "property": "http://www.geneontology.org/formats/oboInOwl#hasExactSynonym",
              "value": "四次葉脈 (Japanese, exact)"
            }
          ]
        }
      ]
    }
    ```
* /getDefinition: *retrieve the defintion string of a matching class in the named ontology in /parse* 
  * HTTP GET  http://{host}/{ontology}/search?user={optional_user}&term={term}&baseIri={baseIri}
  * {ontology}: The ontology to search for the {term}. Ontology must be in lower case, e.g., carex.
  * {optional_user}: If present, the user specific version of the ontology will be used for the search. Otherwise, a shared version of the ontology will be used (See /createUserOntology).
  * {term}: The term to search the definition for
  * {baseIri}: The base iri of the ontology id for the term. The complete ontology id=base_iri#term.
  * Example: GET http://shark.sbs.arizona.edu:8080/carex/getDefinition?baseIri=http://biosemantics.arizona.edu/ontologies/carex&term=apex (this works only after a call to /parse with an empty user and carex ontology as parameters, and term in /parse has an ontology id)
  * Response Body: the definition as a tring
  
 
* /class: *Creates a class in the named ontology*
  * HTTP POST http://{host}/class
  * Request body: If user value is empty, a shared ontology will be used. Otherwise, a user-specific version of the ontology will be used (See /createUserOntology). Fields elucidation and logicDefintion are optional.
    ```json
    {
     	"user":"2",
     	"ontology":"exp",
      "term": "root-apex",
      "superclassIRI": "http://biosemantics.arizona.edu/ontologies/carex#apex",
      "definition": "the apex of the root",
      "elucidation": "http://some.illustration.of/the/apex-root.jpg"
      "createdBy": "hongcui"
      "creationDate": "09-18-2017"
      "definitionSrc": "hongcui"
      "examples": "root apex blah blah blah, used in taxon xyz"
      "logicDefinition": "'root apex' and 'part of' some root"
    }
    ```

  * The response body will be either 
    * IRI of the newly created clas
    * UNSUCCESSFULLY
    * NO_OPERATION (NO_OPERATION means the class already exists and nothing need to be done)
    * Error message in case of logic definition parsing failure.

  * Response Body:
    ```json
    {IRI}|UNSUCCESSFULLY|NO_OPERATION
    ```

* /esynonym: *Creates an exact synonym to the class in the named ontology*
  * HTTP POST http://{host}/esynonym
  * Request body: If user value is empty, a shared ontology will be used. Otherwise, a user-specific version of the ontology will be used (See /createUserOntology).
    ```json
    {
      "user":"2",
	     "ontology":"exp",
      "term": "root-tip",
      "classIRI": "http://biosemantics.arizona.edu/ontologies/carex#root-apex"
    }
    ```

  * Response Body:
    ```json
    SUCCESSFULLY|UNSUCCESSFULLY|NO_OPERATION
    ```

* /bsynonym: *Creates a broader synonym to the class in the named ontology*
  * HTTP POST http://{host}/bsynonym
  * Request body:If user value is empty, a shared ontology will be used. Otherwise, a user-specific version of the ontology will be used (See /createUserOntology).
    ```json
    {
     	"user":"2",
     	"ontology":"exp",
      "term": "root-tip",
      "classIRI": "http://biosemantics.arizona.edu/ontologies/carex#root-apex"
    }
    ```

  * Response Body:
    ```json
    SUCCESSFULLY|UNSUCCESSFULLY|NO_OPERATION
    ```
    
* /definition: *add a defintion property to the class in the named ontology*
  * HTTP POST http://{host}/definition
  * Request body:If user value is empty, a shared ontology will be used. Otherwise, a user-specific version of the ontology will be used (See /createUserOntology).
    ```json
    {
     	"user":"2",
     	"ontology":"exp",
      "definition": "the summit of a root",
      "providedBy": "hongcui",
      "exampleSentence": "root apex rounded",
      "classIRI": "http://biosemantics.arizona.edu/ontologies/carex#root-apex"
    }
    ```

  * Response Body:
    ```json
    SUCCESSFULLY|UNSUCCESSFULLY|NO_OPERATION
    ```

* /comment: *add a rdfs:comment property to the class in the named ontology*
  * HTTP POST http://{host}/comment
  * Request body:If user value is empty, a shared ontology will be used. Otherwise, a user-specific version of the ontology will be used (See /createUserOntology).
    ```json
    {
     	"user":"2",
     	"ontology":"exp",
      "comment": "not sure this term covers my example",
      "providedBy": "hongcui",
      "exampleSentence": "root ends rounded",
      "classIRI": "http://biosemantics.arizona.edu/ontologies/carex#root-apex"
    }
    ```

  * Response Body:
    ```json
    SUCCESSFULLY|UNSUCCESSFULLY|NO_OPERATION
    ```

* /partOf: *Creates a part-of relation between the part and the bearer (part is 'part_of' bearer) in the named ontology*
  * HTTP POST http://{host}/partOf
  * Request body: If user value is empty, a shared ontology will be used. Otherwise, a user-specific version of the ontology will be used (See /createUserOntology).
    ```json
    {
    	 "user":"2",
	     "ontology":"exp",
      "bearerIRI": "http://biosemantics.arizona.edu/ontologies/carex#root",
      "partIRI": "http://biosemantics.arizona.edu/ontologies/carex#apex"
    }
    ```

  * Response Body:
    ```json
    SUCCESSFULLY|UNSUCCESSFULLY|NO_OPERATION
    ```

* /hasPart: *Creates a has-part relation between the bearer and the part (bearer 'has part' part) in the named ontology. *
  * HTTP POST <host>/hasPart
  * Request body:If user value is empty, a shared ontology will be used. Otherwise, a user-specific version of the ontology will be used (See /createUserOntology).
 
    ```json
    {
      "user":"2",
	     "ontology":"exp",
      "bearerIRI": "http://biosemantics.arizona.edu/ontologies/carex#root",
      "partIRI": "http://biosemantics.arizona.edu/ontologies/carex#apex"
    }
    ```
 
  * Response Body:
    ```json
    SUCCESSFULLY|UNSUCCESSFULLY|NO_OPERATION
    ``` 
* /save: *Persists the current state of the named ontology to the file system*
  * HTTP POST <host>/save
  * Request body:If user value is empty, the shared ontology will be saved. Otherwise, a user-specific version of the ontology will be saved (See /createUserOntology).
 
    ```json
    {
      "user":"2",
	     "ontology":"exp",
    }
    ```
    
    
* /{ontology}/getSubclasses: *Obtain the subclasses of the term as a JSON object*
  * HTTP  http://{host}/{ontology}/getSubclasses?user={optional_user}&baseIri={baseIri}&term={term}
  * {ontology}: The ontology to search for the {term}. Ontology must be in lower case, e.g., carex.
  * {optional_user}: If present, the user specific version of the ontology will be used for the search. Otherwise, a shared version of the ontology will be used (See /createUserOntology).
  * {term}: The term for which to find its subclasses
  * {baseIri}: The base iri of the ontology id for the term. The complete ontology id of the term =base_iri#term.
  * Example: GET http://shark.sbs.arizona.edu:8080/carex/getSubclasses?baseIri=http://biosemantics.arizona.edu/ontologies/carex&term=coloration 
  * Response body:
  ```json
  {
    "data": {
        "details": [
            {
                "IRI": "http://biosemantics.arizona.edu/ontologies/carex#coloration"
            }
        ]
    },
    "children": [
        {
            "data": {
                "details": [
                    {
                        "IRI": "http://biosemantics.arizona.edu/ontologies/carex#reddish"
                    }
                ]
            },
            "children": [
                {
                    "data": {
                        "details": [
                            {
                                "IRI": "http://biosemantics.arizona.edu/ontologies/carex#dotted-reddish"
                            }
                        ]
                    },
                    "text": "dotted reddish"
                },
  ```

*  /{ontology}/getTree: *Obtain the entire ontology as a JSON object*
  * HTTP GET http://{host}/{ontology}/getTree?user={optional_user}
  * {ontology}: The ontology content to obtain. Ontology name must be in lower case, e.g., exp.
  * {user}: If present, the user-specific version of the ontology will be used. Otherwise, a shared version of the ontology will be used (See /createUserOntology).
  * Example: GET http://shark.sbs.arizona.edu:8080/carex/getTree (this works only after a call to /createUserOntology with an empty user and carex ontology as parameters)
  * Response body: 
    ```json
    {
    "data": {
        "details": [
            {
                "IRI": "http://www.w3.org/2002/07/owl#Thing"
            }
        ]
    },
    "children": [
        {
            "data": {
                "details": [
                    {
                        "IRI": "http://purl.obolibrary.org/obo/UBERON_0001062"
                    }
     ```
    
