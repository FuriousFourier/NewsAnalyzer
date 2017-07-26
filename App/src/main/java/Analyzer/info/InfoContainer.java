package Analyzer.info;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by pawel on 19.07.17.
 */
public class InfoContainer {

	public static final String[] newspapersNames = {
			"South China Morning Post",
			"Le Monde",
			"The Times of India",
			"El Universal",
			"The New York Times",
			"The Australian",
			"Herald Sun",
			"The Star",
			"China Daily",
			"Daily Telegraph",
			"The Guardian",
			"Hindustan Times",
			"Japan Times",
			"Times of Malta",
			"The Star(malaise)",
			"This Day",
			"New Zealand Herald",
			"The News International",
			"Today",
			"Washington Post",
			"Chronicle",
			"Le Nacion",
			"La Razon",
			"La patria",
			"El mercurio",
			"La tercera",
			"El periodico de Catalunya",
			"El Pais",
			"La Jordana (Mex)",
			"El Universal(MEX)",
			"El Universal",
			"Dernière Heure",
			"Le soir",
			"Le Journal de Montreal",
			"El Watan",
			"LExpression",
			"Le Parisien",
	};

	public static final String[] nonGeomediaNewspapersNames = {
			"Do rzeczy",
			"Fakt",
			"Interia",
			"Newsweek",
			"RMF24",
			"TVN24",
			"Wirtualna Polska",

			"Rzeczpospolita",
			"Gazeta wyborcza",
			"Wprost",
			"Newsweek",
			"TVN24",
			"TVP"
	};

	public static final String[] feedsNames = {
			"fr_FRA_lmonde_int",
			"en_CHN_mopost_int",
			"en_IND_tindia_int",
			"es_MEX_univer_int",
			"en_USA_nytime_int",
			"en_AUS_austra_int",
			"en_AUS_hersun_int",
			"en_CAN_starca_int",
			"en_CHN_chinad_int",
			"en_GBR_dailyt_int",
			"en_GBR_guardi_int",
			"en_IND_hindti_int",
			"en_JPN_jatime_int",
			"en_MLT_tmalta_int",
			"en_MYS_starmy_int",
			"en_NGA_thiday_int",
			"en_NZL_nzhera_int",
			"en_PAK_newint_int",
			"en_SGP_twoday_int",
			"en_USA_wapost_int",
			"en_ZWE_chroni_int",
			"es_ARG_nacion_int",
			"es_BOL_larazo_int",
			"es_BOL_patria_int",
			"es_CHL_mercur_int",
			"es_CHL_tercer_int",
			"es_ESP_catalu_int",
			"es_ESP_elpais_int",
			"es_MEX_jormex_int",
			"es_MEX_univer_int",
			"es_VEN_univer_int",
			"fr_BEL_derheu_int",
			"fr_BEL_lesoir_int",
			"fr_CAN_jmontr_int",
			"fr_DZA_elwata_int",
			"fr_DZA_xpress_int",
			"fr_FRA_lepari_int",
	};

	public static final String[] nonGeomediaFeedsNames = {
			"pl_POL_dorzeczy_int",
			"pl_POL_fakt_int",
			"pl_POL_interia_int",
			"pl_POL_newsweek_int",
			"pl_POL_rmf24_int",
			"pl_POL_tvn24_int",
			"pl_POL_wp_int",

			"pl_POL_rp_int",
			"pl_POL_wyborcza_int",
			"pl_POL_wprost_int",
			"pl_POL_newsweek_2_int",
			"pl_POL_tvn24_2_int",
			"pl_POL_tvp_int",

	};

	public static final String[] newspapersCountry = {
			"China",
			"France",
			"India",
			"Mexico",
			"United States of America",
			"Australia",
			"Australia",
			"Canada",
			"China",
			"United Kingdom",
			"United Kingdom",
			"India",
			"Japan",
			"Malta",
			"Malaysia",
			"Nigeria",
			"New Zealand",
			"Pakistan",
			"Singapore",
			"United States of America",
			"Zimbabwe",
			"Argentina",
			"Bolivia",
			"Bolivia",
			"Chile",
			"Chile",
			"Spain",
			"Spain",
			"Mexico",
			"Mexico",
			"Venezuela",
			"Belgium",
			"Belgium",
			"Canada",
			"Algeria",
			"Algeria",
			"France",
	};

	public static final String[] nonGeomediaNewspapersCountry = {
			"Poland",
			"Poland",
			"Poland",
			"Poland",
			"Poland",
			"Poland",
			"Poland",

			"Poland",
			"Poland",
			"Poland",
			"Poland",
			"Poland",
			"Poland"
	};

	public static final String[] newspapersLanguage = {
			"English",
			"French",
			"English",
			"Spanish",
			"English",
			"English",
			"English",
			"English",
			"English",
			"English",
			"English",
			"English",
			"English",
			"English",
			"English",
			"English",
			"English",
			"English",
			"English",
			"English",
			"English",
			"Spanish",
			"Spanish",
			"Spanish",
			"Spanish",
			"Spanish",
			"Spanish",
			"Spanish",
			"Spanish",
			"Spanish",
			"Spanish",
			"French",
			"French",
			"French",
			"French",
			"French",
			"French",
	};

	public static final String[] nonGeomediaNewspapersLanguage = {
			"Polish",
			"Polish",
			"Polish",
			"Polish",
			"Polish",
			"Polish",
			"Polish",

			"Polish",
			"Polish",
			"Polish",
			"Polish",
			"Polish",
			"Polish"
	};

	public static final String[] languages = {
			"English",
			"Spanish",
			"French",
			"Polish"
	};

	public static final String DESTINATION_TAGS_FOLDER_PATHS = "./data/TaggedFeeds";
	public static final String NEW_FEEDS_PATH = "./data/Feeds";
	public static final String ORGANIZATION_TAG_FILE_PATH = "./data/datafiles/orgs.csv";
	public static final String ORGANIZATION_SHORT_TAG_FILE_PATH = "./data/datafiles/orgs_short.csv";
	public static final String TAGS_AND_COUNTRIES_FILE_PATH = "./data/datafiles/tagsAndCountries.csv";
	public static final String COUNTRY_TAG_FILE_NAME = "Dico_Country_Free.csv";
	public static final String EBOLA_TAG_FILE_NAME = "Dico_Ebola_Free.csv";
	public static final String GEOMEDIA_EBOLA_TAGGED_FILE_NAME = "rss_unique_TAG_country_Ebola.csv";
	public static final String GEOMEDIA_RSS_FILE_NAME = "rss.csv";
	public static final String GEOMEDIA_UNIQUE_FILE_NAME = "rss_unique.csv";
	public static final String ORG_TAGGED_FILE_NAME = "rss_org_tagged.csv";
	public static final String STEMMING_FOLDER_PATH = "./data/datafiles";
	public static final String COUNTRIES_FILE_PATH = "../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB/countries.csv";

	public static final String oldFeedsFolderPaths = "./data/Geomedia_extract_AGENDA";
	public static final String countryTagFile = "./data/datafiles/Dico_Country_Free.csv";
	public static final String[] currencyTagFiles = {"./data/datafiles/currencies_Polish.csv",
			"./data/datafiles/currencies_English.csv"};
}
