library(plotly)

folder <- "../data/ORGTagsForNewspapers/"
output_folder <- "../plots/plots_org/"

file_list <- list.files(path = folder, pattern = "*.csv")
list <- vector(mode = 'list', length = length(file_list))
for (i in 1:length(file_list)){
  list[[i]] <- read.csv(paste(folder, file_list[i], sep = ""), header = FALSE, sep = "\t")
}

top <- 10

for (i in 1:length(file_list)) {
  data <- list[[i]]
  data <- head(data[order(data$V3, decreasing = TRUE),], top)
  data <- data[order(data$V2),]
  
  name <- gsub(".csv", "", file_list[i])
  
  p <- plot_ly(
    x = factor(data$V2, levels = data$V2),
    y = data$V3,
    type = "bar",
    text = text
  ) %>%
    layout(
      title = name,
      xaxis = list(title = "", tickangle = -45),
      yaxis = list(title = ""),
      margin = list(b = 120)
    )
  
  export(p, file=paste(output_folder, name, ".pdf", sep = ""))
}