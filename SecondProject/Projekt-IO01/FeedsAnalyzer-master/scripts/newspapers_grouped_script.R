library(plotly)

folder <- "../data/QuantityOfTagsForNewspapersGroupedByCountry/"
output_folder <- "../plots/plots_newspapers_grouped/"

file_list <- list.files(path = folder, pattern = "*.csv")
list <- vector(mode = 'list', length = length(file_list))
for (i in 1:length(file_list)){
  list[[i]] <- read.csv(paste(folder, file_list[i], sep = ""), header = FALSE, sep = "\t")
}

top <- 10

for (i in 1:length(file_list)) {
  newspaper_data <- list[[i]]
  
  name <- as.character(newspaper_data$V1[1])
  aggregated_countries <- aggregate(V4 ~ V3, newspaper_data, sum)
  top_countries <- head(aggregated_countries[order(aggregated_countries$V4, decreasing = TRUE),], top)
  top_countries <- as.vector(top_countries$V3)
  newspaper_data <- subset(newspaper_data, newspaper_data$V3 %in% top_countries)
  data_list <- split(newspaper_data, newspaper_data$V2)
  data_list <- lapply(data_list, function(df) {
                        df[order(df$V3, decreasing = TRUE),]
                      })
  data <- data.frame(Country = c(unique(as.character(newspaper_data[order(newspaper_data$V3, decreasing = TRUE),]$V3)))) 
  for (j in 1:length(data_list)) {
    data <- cbind.data.frame(data, data_list[[j]]$V4)
    names(data)[j + 1] <- paste("V", j + 1, sep = "")
  }

  p <- plot_ly(data, x = ~Country, y = ~data[[2]], type = "bar", name = data_list[[1]]$V2[1])
    if (length(data_list) > 1) {
      for (j in 2:length(data_list)) {
        p <- add_trace(p, y = ~data[[j + 1]], name = data_list[[j]]$V2[1])
      }
    }
  p <- layout(
      p,
      title = name,
      xaxis = list(title = "", tickangle = -45),
      yaxis = list(title = ""),
      barmode = "stack",
      margin = list(b = 120)
    )
  
  export(p, file=paste(output_folder, name, ".pdf", sep = ""))
}