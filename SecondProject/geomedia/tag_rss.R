## Packages nécessaires à charger avant le lancement de la fonction :
library(tm)
library(plyr)
#####################################################################



## Programme tag_rss:
## Fonction package = tag_rss ( RawDataFolder="/test",
##     WorkDirectory="/test", 
##     SelectFiles="NameList",
##     NameDico="Pays",
##     Accuracy=FALSE,
##     TagText1=TRUE,
##     TagText2=TRUE )



## PARAMETRES :

## File path to the destination to the working directory, compulsory, character.
WorkDirectory="~/programming/geomedia_3D/program/"

## The name of an existing list of feeds WHICH HAVE BEEN CLEAN, compulsory, character:
SelectList="test_script"

## The name of an existing dictionnary (in the commons) , compulsory, character:
NameDico="Dico_Ebola_Free"

## TRUE or FALSE. Indicate which words have to be found ('exact' words ==TRUE OR 'exact+approx' words ==FASLE), compulsory, boolean.
Accuracy=FALSE

## TRUE or FALSE. Indicate if the variable text1 have to be used to find TAG, compulsory, boolean.
TagText1=TRUE

## TRUE or FALSE. Indicate if the variable text2 have to be used to find TAG, compulsory, boolean.
TagText2=FALSE





## NE PAS MODIFIER LE CODE CI-DESSOUS :

## Fonction tag_rss:

####################### Lecture des métadonnées #######################

## Ouverture du fichier de métadonnées sur tous les flux RSS (Global_metadata)
metadata <- read.table(file=paste(WorkDirectory,SelectList,"/X.Global_metadata.csv", sep=""),
                       header=TRUE,sep="\t",
                       stringsAsFactors=FALSE,
                       encoding="UTF-8")

## Récupération du nom des colonnes de la table 'metadata'
ColNameMetadata<-colnames(metadata)

## copie de la table des metadata (réutiliser au moment de la création de nouvelles metadata)
met<-metadata


######################################################################


################ Lecture du dictionnaire séléctionné ##################

dico <- read.csv(paste(WorkDirectory,SelectList,"/",NameDico,".csv", sep=""),
                 header=TRUE,sep="\t",
                 dec=".", stringsAsFactors=FALSE,
                 encoding="UTF-8")


## Selection du dico en fonction de la précision de taggage à réaliser
if (Accuracy) {sel_dico <- dico[dico$ACCURACY==TRUE,]
    precision<-"exact"} else {sel_dico <- dico
                          precision<-"Approx"}   

## Récupération des langues diponibles dans le dico
Nb_LG<-ddply(sel_dico, .(LG), summarise, List_LG = length(LG))
list_LG<-c(Nb_LG$LG)
######################################################################



############ Boucle de creation des fichier nettoyés (rss) ############

## Lecture, un par un, des fichier de données brutes:
flux<-data.frame(code=metadata$Name_Flux)
flux$code<-as.character(flux$code)
nbflux<-dim(flux)[1]-1

i<-1
for (i in 1:nbflux) { 
    
    ## Récupération nom code du flux
    fluxName<-flux$code[i]
    ## Récupération de la langue utilisé dans le rss
    langue<-substr(fluxName, 1, 2)
    

    ## les données brutes de chaque flux sont stockés dans la table 'rss'
    rss <- read.table(file.path(paste(WorkDirectory,SelectList,"/",fluxName,"/rss_unique.csv",sep="")),
                      header=TRUE,sep="\t",
                      stringsAsFactors=FALSE,
                      encoding="UTF-8") 

    
    if (langue %in% list_LG) {
        
        
############### Creation d'un nouveau champ = text à tagger ##############

        ## Récupération du text à tagger en fonction des choix de l'utilisateur
        if (TagText1) {
            if (TagText2) {
                rss$TextToTag <-do.call(paste0, c(" ", rss[4], " " , rss[5], " "))
                NomFichier<-"T1andT2"
            } else {                
                rss$TextToTag <-do.call(paste0, c(" ", rss[4], " "))
                NomFichier<-"T1"
            }
        } else {
            if (TagText2) {rss$TextToTag <-do.call(paste0, c(" ", rss[5], " "))
                NomFichier<-"T2"
            } else {
                print("TagText1 or TagText1 have to be = TRUE")
            }
        }

#########################################################################

###### Selection dico par langue + Traitement des mots à rechercher ######

        ## Selection des mots à rechercher dans la langue du flux
        sel_dico_LG<-sel_dico[sel_dico$LG==langue,]


#########################################################################
################## MODIFIER ! ############################################
#########################################################################

        ## Traitement des mot à rechercher (mise en minuscule...) pour les TAGs pays uniquement
        sel_dico_LG$WORD <- gsub(x = sel_dico_LG$WORD, pattern = "[^[:alnum:][:space:]']", replacement = " ")
        sel_dico_LG$WORD <- gsub(x = sel_dico_LG$WORD, pattern = "[[:punct:]]", replacement = " ")
        sel_dico_LG$WORD <- tolower(sel_dico_LG$WORD)
        sel_dico_LG$WORD <- stripWhitespace(sel_dico_LG$WORD)

#########################################################################

##################### Traitement du texte à analyser #####################

        ## Remplacement de toute la ponctuaction par " "
        rss$TextToTag <- gsub(x = rss$TextToTag, pattern = "[^[:alnum:][:space:]']", replacement = " ")
        rss$TextToTag <- gsub(x = rss$TextToTag, pattern = "[[:punct:]]", replacement = " ")
        rss$TextToTag <-tolower(rss$TextToTag)
        rss$TextToTag <-stripWhitespace(rss$TextToTag)


#########################################################################
#########################################################################
#########################################################################


################################ TAGGAGE #################################

        Dim_Dico<-dim(sel_dico_LG)[1]
        TAG<-as.data.frame(NULL)
        indArt<-NULL
        r<-1
        for (r in 1:Dim_Dico) {
            indArt<-grep(paste(" ",sel_dico_LG$WORD[r]," ",sep=""), rss$TextToTag,fixed=TRUE,value=FALSE)
            if(length(indArt)!= 0) {
                t1<-cbind(rss[indArt,1],rss[indArt,2],rss[indArt,3],rss[indArt,4],rss[indArt,5],sel_dico_LG$WORD[r], sel_dico_LG$TAG[r], sel_dico_LG$TYPE[r],sel_dico_LG$ACCURACY[r])
                TAG<-rbind(TAG,t1)
            }
        }


        if (nrow(TAG)!=0) {

            ## Ajout nom de colonne de la table TAG
            colnames(TAG)<-c("ID","Flux","time","Text1","Text2","word","TAG","Type","Accuracy")



##### ECRITURE DE LA TABLE DES RESULTAT = une ligne par "word" trouvé
## Creation du dossier de resultat des TAG
## Test pour savoir si le dossier existe
            if (file.access(paste(WorkDirectory,SelectList,"/",substr(fluxName,1,17),"/Results_Tags/",sep=""), mode = 0)!=0) {
                ##Creation du dossier 'Result_Tags'
                Results_Tags<-paste(WorkDirectory,SelectList,"/",substr(fluxName,1,17),"/Results_Tags/",sep="")
                dir.create(Results_Tags)
            }


            ##Test pour savoir si le dossier existe
            if (file.access(paste(WorkDirectory,SelectList,"/",fluxName,"/Results_Tags/",NameDico ,sep=""), mode = 0)!=0) { 
                ##Creation du dossier 'commons'
                DicoFolder<-paste(WorkDirectory,SelectList,"/",fluxName,"/Results_Tags/",NameDico ,sep="")
                dir.create(DicoFolder)
            }



            ## Ecriture de la table des résultats "tous les Tags & word"
            folder<-paste(WorkDirectory,SelectList,"/",fluxName,"/Results_Tags/",NameDico,"/",sep="")
            write.table(TAG, file=paste(folder,NameDico,"_",precision,"_", NomFichier,"_by_WORD.csv", sep=""),
                        col.names=TRUE,
                        row.names=FALSE,
                        sep="\t",
                        qmethod="double",
                        fileEncoding="UTF-8")




##### ECRITURE DE LA TABLE DES RESULTAT = une ligne par "TAG" trouvé 

            TAG$TAG<-as.character(TAG$TAG)
            TAG$word<-as.character(TAG$word)
            TAG$Accuracy<-as.character(TAG$Accuracy)
            TAG$Type<-as.character(TAG$Type)


            ## Reroupement par Item
            TAGTAG<-ddply(TAG, .(ID, TAG), summarise,Nb_TAG_SIM = length(TAG))
            TAGTAG <- merge(TAGTAG,rss, by="ID", all.x=TRUE)
            TAGTAG <- TAGTAG[, c(1,4,5,6,7,2,3)]
            colnames(TAGTAG)<-c("ID","Flux","time","Text1","Text2","TAG","Nb_TAG_SIM")


            ## Ecriture de la table des résultats "tous les Tags & word"
            folder<-paste(WorkDirectory,SelectList,"/",fluxName,"/Results_Tags/",NameDico,"/",sep="")
            write.table(TAGTAG, file=paste(folder,NameDico,"_",precision,"_", NomFichier,"_by_TAG.csv", sep=""), 
                        col.names=TRUE,
                        row.names=FALSE,
                        sep="\t",
                        qmethod="double",
                        fileEncoding="UTF-8")




##### ECRITURE DE LA TABLE DES RESULTAT = une ligne par "TAG" trouvé AVEC TOUS LES ITEMS 


            FULL_RSS_TAG <- merge(rss,TAGTAG, by="ID", all.x=TRUE)
            FULL_RSS_TAG <- FULL_RSS_TAG[, c(1:5,11:12)]
            colnames(FULL_RSS_TAG) <-c("ID","Flux","time","Text1","Text2","TAG","Nb_TAG_SIM")
            FULL_RSS_TAG[is.na(FULL_RSS_TAG)] <- ""


            ## Ecriture de la table des résultats "TOUS les items + TAG"
            folder<-paste(WorkDirectory,SelectList,"/",fluxName,"/Results_Tags/",NameDico,"/",sep="")
            write.table(FULL_RSS_TAG, file=paste(folder,NameDico,"_",precision,"_", NomFichier,"_FULL_RSS_by_TAG.csv", sep=""), 
                        col.names=TRUE,
                        row.names=FALSE,
                        sep="\t",
                        qmethod="double",
                        fileEncoding="UTF-8")





##### ECRITURE DE LA TABLE DES RESULTATS = une ligne par item 


            ## Reroupement par Item
            TAG2<-ddply(TAG, .(ID), summarise, Nb_word_Unique = length(unique(word)), listword = list(word), Nb_TAG_Unique = length(unique(TAG)), listTAG = list(TAG), listtype = list(Type), listaccuracy = list(Accuracy))


            ## Création de nouvelles variables
            TAG2$List_Word<-""
            TAG2$List_TAG<-""
            TAG2$List_type<-""
            TAG2$List_accuracy<-""

            h<-1
            c<-as.vector(NULL)
            for (h in h:nrow(TAG2)) { 
                
                if (length(TAG2$listword[[h]])!=1) {
                    
                    TAG2$List_Word[h]<-paste(c( TAG2$listword[[h]][1:length(TAG2$listword[[h]])]), collapse = "|")
                    TAG2$List_TAG[h]<-paste(c( TAG2$listTAG[[h]][1:length(TAG2$listTAG[[h]])]), collapse = "|") 
                    TAG2$List_type[h]<-paste(c( TAG2$listtype[[h]][1:length(TAG2$listtype[[h]])]), collapse = "|")
                    TAG2$List_accuracy[h]<-paste(c( TAG2$listaccuracy[[h]][1:length(TAG2$listaccuracy[[h]])]), collapse = "|")
                    
                } else {
                    
                    TAG2$List_Word[h]<- TAG2$listword[[h]] 
                    TAG2$List_TAG[h]<-TAG2$listTAG[[h]]
                    TAG2$List_type[h]<-TAG2$listtype[[h]]
                    TAG2$List_accuracy[h]<-TAG2$listaccuracy[[h]]
                    
                }
            } 


            ## Récupération métadonnées

            ## Récupération Nb de "TAG" doublon pour chaque item
            Nb_SameTag<-ddply(TAG, .(ID, TAG), summarise, Nb_TAG_similaires = length(ID))
            Nb_SameTag<-Nb_SameTag[Nb_SameTag$Nb_TAG_similaires>1,]
            Nb_SameTag$Nb_TAG_similaires<-Nb_SameTag$Nb_TAG_similaires-1

            if(nrow(Nb_SameTag)!=0) {
                Nb_SameTag<-ddply(Nb_SameTag, .(ID), summarise, Nb_TAG_Doublon = sum(Nb_TAG_similaires))
                ## Jointure table result par item avec métadonnée par item
                TAG2<-merge(TAG2,Nb_SameTag, by="ID", all.x=TRUE)
            } else {
                TAG2$Nb_TAG_Doublon<-0
            }
            
            

            ## Récupération Nb de "word" similaires pour chaque item
            Nb_SameWord<-ddply(TAG, .(ID, word), summarise, Nb_WORD_similaires = length(ID))
            Nb_SameWord<-Nb_SameWord[Nb_SameWord$Nb_WORD_similaires>1,]
            Nb_SameWord$Nb_WORD_similaires<-Nb_SameWord$Nb_WORD_similaires-1

            if(nrow(Nb_SameWord)!=0) {
                Nb_SameWord<-ddply(Nb_SameWord, .(ID), summarise, Nb_WORD_Doublon = sum(Nb_WORD_similaires))
                ## Jointure table result par item avec métadonnée par item
                TAG2<-merge(TAG2,Nb_SameWord, by="ID", all.x=TRUE)
            } else {
                TAG2$Nb_WORD_Doublon<-0
            }
            
            


            ##Ajout du nom code du flux
            TAG2$Flux<-fluxName


            ## Nettoyage de la table
            TAG_by_Item<-TAG2[,c(1,14,2,12,8,4,13,9,10,11)]
            ## TAG_by_Item[is.na(TAG_by_Item)] <- 0


            ## Ecriture de la table des résultats "Tag by Item"
            folder<-paste(WorkDirectory,SelectList,"/",fluxName,"/Results_Tags/",NameDico,"/",sep="")
            write.table(TAG_by_Item, file=paste(folder,NameDico,"_",precision,"_", NomFichier,"_by_ITEM.csv", sep=""),
                        col.names=TRUE,
                        row.names=FALSE,
                        sep="\t",
                        qmethod="double",
                        fileEncoding="UTF-8")





##### ECRITURE DE LA TABLE DES RESULTATS = une ligne par jour -> dans metadata

            ## Création d'un champ Jour
            TAG$jour<-as.Date(substr(TAG$time,1,10),format="%Y-%m-%d")


            ## Reroupement par jour
            TAG3<-ddply(TAG, .(jour), summarise, Nb_word_Unique = length(unique(word)), listword = list(word), Nb_TAG_Unique = length(unique(TAG)), listTAG = list(TAG), listtype = list(Type), listaccuracy = list(Accuracy))


            ## Création de nouvelles variables
            TAG3$List_Word<-""
            TAG3$List_TAG<-""
            TAG3$List_type<-""
            TAG3$List_accuracy<-""

            h<-1
            c<-as.vector(NULL)
            for (h in h:nrow(TAG3)) { 
                
                if (length(TAG3$listword[[h]])!=1) {
                    
                    TAG3$List_Word[h]<-paste(c( TAG3$listword[[h]][1:length(TAG3$listword[[h]])]), collapse = "|")
                    TAG3$List_TAG[h]<-paste(c( TAG3$listTAG[[h]][1:length(TAG3$listTAG[[h]])]), collapse = "|") 
                    TAG3$List_type[h]<-paste(c( TAG3$listtype[[h]][1:length(TAG3$listtype[[h]])]), collapse = "|")
                    TAG3$List_accuracy[h]<-paste(c( TAG3$listaccuracy[[h]][1:length(TAG3$listaccuracy[[h]])]), collapse = "|")
                    
                } else {
                    
                    TAG3$List_Word[h]<- TAG3$listword[[h]] 
                    TAG3$List_TAG[h]<-TAG3$listTAG[[h]]
                    TAG3$List_type[h]<-TAG3$listtype[[h]]
                    TAG3$List_accuracy[h]<-TAG3$listaccuracy[[h]]
                    
                } 
                
            } 


            ## Récupération métadonnées

            ## Récupération Nb de "TAG" doublons pour chaque Jour
            Nb_SameTag_Samejour<-ddply(TAG, .(jour, TAG), summarise, Nb_TAG_similaires = length(TAG))
            Nb_SameTag_Samejour<-Nb_SameTag_Samejour[Nb_SameTag_Samejour$Nb_TAG_similaires>1,]
            Nb_SameTag_Samejour$Nb_TAG_similaires<-Nb_SameTag_Samejour$Nb_TAG_similaires-1

            if(nrow(Nb_SameTag_Samejour)!=0) {
                Nb_SameTag_Samejour<-ddply(Nb_SameTag_Samejour, .(jour), summarise, Nb_TAG_Doublon = sum(Nb_TAG_similaires))
                ## Jointure table result par item avec métadonnée par item
                TAG3<-merge(TAG3,Nb_SameTag_Samejour, by="jour", all.x=TRUE)
            } else {
                TAG3$Nb_TAG_Doublon<-0
            }

            ## Récupération Nb de "word" similaires chaque Jour
            Nb_SameWord_Samejour<-ddply(TAG, .(jour, word), summarise, Nb_WORD_similaires = length(word))    
            Nb_SameWord_Samejour<-Nb_SameWord_Samejour[Nb_SameWord_Samejour$Nb_WORD_similaires>1,]
            Nb_SameWord_Samejour$Nb_WORD_similaires<-Nb_SameWord_Samejour$Nb_WORD_similaires-1

            if(nrow(Nb_SameWord_Samejour)!=0) {
                Nb_SameWord_Samejour<-ddply(Nb_SameWord_Samejour, .(jour), summarise, Nb_WORD_Doublon = sum(Nb_WORD_similaires))      
                ## Jointure table result par item avec métadonnée par item
                TAG3<-merge(TAG3,Nb_SameWord_Samejour, by="jour", all.x=TRUE)
            } else {
                TAG3$Nb_WORD_Doublon<-0
            }



            ## Récupération Nb Item total collectés chaque jour
            rss$jour<-as.Date(substr(rss$time,1,10),format="%Y-%m-%d")
            Total_item<-ddply(rss, .(jour), summarise, Total_item = length(ID))

            TAG3<-merge(TAG3,Total_item, by="jour", all.x=TRUE)

                                        #récupération nb items taggués par jour
            rss_bis<-rss[,c(1,7)]
            TAG_by_Item_with_Jour<-merge(TAG_by_Item, rss_bis, by="ID", all.x=TRUE)
            Nb_ItemTag_by_day<-ddply(TAG_by_Item_with_Jour, .(jour), summarise, Nb_Items_Tagguer = length(ID)) 
            TAG3<-merge(TAG3, Nb_ItemTag_by_day, by="jour", all.x=TRUE)

                                        #Ajout du nom code du flux
            TAG3$Flux<-fluxName

            ## Nettoyage de la table
            TAG_by_Day<-TAG3[,c(1,16,2,12,8, 4,13,9,10,11,15,14)]
            TAG_by_Day[is.na(TAG_by_Day)] <- 0



            ## Ecriture de la table des résultats "Tag by Day"
            folder<-paste(WorkDirectory,SelectList,"/",fluxName,"/Results_Tags/",NameDico,"/",sep="")
            write.table(TAG_by_Day, file=paste(folder,NameDico,"_",precision,"_", NomFichier,"_by_DAY.csv", sep=""), 
                        col.names=TRUE,
                        row.names=FALSE,
                        sep="\t",
                        qmethod="double",
                        fileEncoding="UTF-8")



            ## Ecriture métadonnées dans le fichier "Global_metadata_... .csv"

            ## if (length(unique(TAG_by_Item$Nb_TAG_Unique))==1) {
            ## 
            ## ## Nombre total de "words" uniques par item
            ## if (is.na(match("Nb_Word", names(metadata)))==TRUE) {metadata$Nb_Word<-""}
            ## metadata[metadata$Name_Flux %in% substr(fluxName,1,17), ncol(met)+1 ]<- sum(TAG_by_Item$Nb_word_Unique)
            ## 
            ## ## Nombre total de "TAG" uniques par item
            ## if (is.na(match("Nb_item_tag", names(metadata)))==TRUE) {metadata$Nb_item_tag<-""}
            ## metadata[metadata$Name_Flux %in% substr(fluxName,1,17), ncol(met)+2 ]<- length(TAG_by_Item$ID) 
            ## 
            ## 
            ## 
            ## } else {

            
            ## Nombre total de "words" uniques par item
            if (is.na(match("Nb_Word", names(metadata)))==TRUE) {metadata$Nb_Word<-""}
            metadata[metadata$Name_Flux %in% substr(fluxName,1,17), ncol(met)+1 ]<- sum(TAG_by_Item$Nb_word_Unique)

            ## Nombre total de "TAG" uniques par item
            if (is.na(match("Nb_tag", names(metadata)))==TRUE) {metadata$Nb_tag<-""}
            metadata[metadata$Name_Flux %in% substr(fluxName,1,17), ncol(met)+2 ]<- sum(TAG_by_Item$Nb_TAG_Unique)
            
            ## Nombre total de "TAG" uniques par item
            if (is.na(match("Nb_item_tag", names(metadata)))==TRUE) {metadata$Nb_item_tag<-""}
            metadata[metadata$Name_Flux %in% substr(fluxName,1,17), ncol(met)+3 ]<- length(TAG_by_Item$ID)

            ## }


        } else {
            print (paste("AUCUN TAG trouvé pour le flux: ",substr(fluxName,1,17), sep=""))

           
            
##### ECRITURE DE LA TABLE DES RESULTAT = une ligne par "word" trouvé -> dans metadata
## Creation du dossier de resultat des TAG
## Test pour savoir si le dossier existe
            if (file.access(paste(WorkDirectory,SelectList,"/",substr(fluxName,1,17),"/Results_Tags/",sep=""), mode = 0)!=0) {
                                        #Creation du dossier 'Result_Tags'
                Results_Tags<-paste(WorkDirectory,SelectList,"/",substr(fluxName,1,17),"/Results_Tags/",sep="")
                dir.create(Results_Tags)
                
            }
            
                                        #Test pour savoir si le dossier existe
            if (file.access(paste(WorkDirectory,SelectList,"/",fluxName,"/Results_Tags/",NameDico ,sep=""), mode = 0)!=0) { 
                                        #Creation du dossier 'commons'
                DicoFolder<-paste(WorkDirectory,SelectList,"/",fluxName,"/Results_Tags/",NameDico ,sep="")
                dir.create(DicoFolder)
            }
            
            
            
            
            folder<-paste(WorkDirectory,SelectList,"/",fluxName,"/Results_Tags/",NameDico,"/",sep="")

            TAG <- data.frame(ID=character(),
                              Flux=character(), 
                              time=character(), 
                              Text1=character(),
                              Text2=character(), 
                              word=character(),
                              TAG=character(),
                              Type=character(), 
                              Accuracy=character(),
                              stringsAsFactors=FALSE) 
            
            write.table(TAG, file=paste(folder,NameDico,"_",precision,"_", NomFichier,"_by_WORD.csv", sep=""),
                        col.names=TRUE,
                        row.names=FALSE,
                        sep="\t",
                        qmethod="double",
                        fileEncoding="UTF-8")

            
            
            
            TAGTAG <- data.frame(ID=character(),
                                 Flux=character(), 
                                 time=character(), 
                                 Text1=character(),
                                 Text2=character(),
                                 TAG=character(), 
                                 Nb_TAG_SIM=character(), 
                                 stringsAsFactors=FALSE) 
            
            
            write.table(TAGTAG, file=paste(folder,NameDico,"_",precision,"_", NomFichier,"_by_TAG.csv", sep=""),
                        col.names=TRUE,
                        row.names=FALSE,
                        sep="\t",
                        qmethod="double",
                        fileEncoding="UTF-8")

            
            
            FULL_RSS_TAG <- rss 
            FULL_RSS_TAG$TAG <-""
            FULL_RSS_TAG$Nb_TAG_SIM <-""
            
            write.table(FULL_RSS_TAG, file=paste(folder,NameDico,"_",precision,"_", NomFichier,"_FULL_RSS_by_TAG.csv", sep=""),
                        col.names=TRUE,
                        row.names=FALSE,
                        sep="\t",
                        qmethod="double",
                        fileEncoding="UTF-8")
            

            
            TAG_by_Item <- data.frame(ID=character(),
                                      Flux=character(), 
                                      Nb_word_Unique=character(), 
                                      Nb_TAG_Doublon=character(),
                                      List_Word=character(), 
                                      Nb_TAG_Unique=character(),
                                      Nb_WORD_Doublon=character(),
                                      List_TAG=character(), 
                                      List_type=character(),
                                      List_accuracy=character(),
                                      stringsAsFactors=FALSE) 
            
            write.table(TAG_by_Item, file=paste(folder,NameDico,"_",precision,"_", NomFichier,"_by_ITEM.csv", sep=""),
                        col.names=TRUE,
                        row.names=FALSE,
                        sep="\t",
                        qmethod="double",
                        fileEncoding="UTF-8")
            
            
            
            TAG_by_Day<- data.frame(Jour=character(),
                                    Flux=character(), 
                                    Nb_word_Unique=character(), 
                                    Nb_TAG_Doublon=character(),
                                    List_Word=character(), 
                                    Nb_TAG_Unique=character(),
                                    Nb_WORD_Doublon=character(),
                                    List_TAG=character(), 
                                    List_type=character(),
                                    List_accuracy=character(),
                                    Nb_Items_Tagguer=character(),
                                    Total_item=character(),
                                    stringsAsFactors=FALSE) 
            
            write.table(TAG_by_Day, file=paste(folder,NameDico,"_",precision,"_", NomFichier,"_by_DAY.csv", sep=""),
                        col.names=TRUE,
                        row.names=FALSE,
                        sep="\t",
                        qmethod="double",
                        fileEncoding="UTF-8")
            
            

        }


        ## Message pour suivre l'exécution du programme
        print(paste("Les résultats du taggage des items du flux '", substr(fluxName,1,17), "' sont enregistrés", sep=""))

    } else {
        print (paste("Le dictionnaire ne comporte AUCUN mot dans la langue du flux : ",substr(fluxName,1,17), sep=""))
    }

}



## if (length(unique(TAG_by_Item$Nb_TAG_Unique))==1) {
## 
## 
## colnames(metadata)<-c(ColNameMetadata,paste("Word_",precision,"_found_", NameDico,"_", NomFichier, sep=""),
##     paste("Nb_item_tagguer_dico_", NameDico,"_",precision,"_", NomFichier, sep=""))
## 
## 
## ## Ecriture de la table des metadata (Global_metadata... .csv)
## folder<-paste(WorkDirectory,SelectList,"/",substr(fluxName,1,17),"/Results_Tags/",sep="")
## write.table(metadata, file=folder<-paste(WorkDirectory,SelectList,"/X.Global_metadata_", SelectList,".csv",sep=""),
##  col.names=TRUE,
##  row.names=FALSE,
##  sep="\t",
##  qmethod="double",
##  fileEncoding="UTF-8") 
## 
## 
## 
## } else {

colnames(metadata)<-c(ColNameMetadata,paste("Word_",precision,"_found_", NameDico,"_", NomFichier, sep=""),
                      paste("TAG_found_", NameDico,"_", NomFichier, sep=""),
                      paste("Nb_item_tagguer_dico_", NameDico,"_",precision,"_", NomFichier, sep=""))



## Ecriture de la table des metadata (Global_metadata... .csv)
write.table(metadata, file=folder<-paste(WorkDirectory,SelectList,"/X.Global_metadata_", SelectList,".csv",sep=""),
            col.names=TRUE,
            row.names=FALSE,
            sep="\t",
            qmethod="double",
            fileEncoding="UTF-8") 

## }


## Message pour suivre l'exécution du programme
print(paste("Des variables ont été ajoutées dans le fichier de métadonnées 'X.Global_metadata_", SelectList,".csv'", sep=""))
print(paste("Le taggage des items avec le dico '", NameDico , "' est terminé", sep=""))
