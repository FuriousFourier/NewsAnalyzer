library(plotly)

folder <- "../data/NewFeeds/TagsForNewspapers/"
output_folder <- "../plots/plots_newspapers_percent_newfeeds/"

file_list <- list.files(path = folder, pattern = "*.csv")
list <- vector(mode = 'list', length = length(file_list))
for (i in 1:length(file_list)){
  list[[i]] <- read.csv(paste(folder, file_list[i], sep = ""), header = FALSE, sep = "\t")
}

top <- 5

for (i in 1:length(file_list)) {
  newspaper_data <- list[[i]]
  
  name <- as.character(newspaper_data$V1[1])
  
  newspaper_data <- head(newspaper_data[order(newspaper_data$V3, decreasing = TRUE),], top)
  newspaper_data <- cbind(newspaper_data, "V4" = 'rgb(31, 119, 180)')
  others_sum <- sum(tail(list[[1]][order(list[[1]]$V3, decreasing = TRUE),], (-top))$V3)
  others_sum <- cbind.data.frame(newspaper_data$V1[1], "Others", others_sum, 'rgb(224, 224, 224)')
  colnames(others_sum) <- c("V1", "V2", "V3", "V4")
  newspaper_data <- rbind(newspaper_data, others_sum)
  newspaper_data <- data.frame("country" = newspaper_data$V2, "value" = newspaper_data$V3, "color" = newspaper_data$V4)
  
  p <- plot_ly(newspaper_data, labels = ~country, values = ~value, type = "pie",
               textposition = "inside",
               textinfo = "label+percent",
               insidetextfont = list(color = '#FFFFFF'),
               marker = list(colors = newspaper_data$color,
                             line = list(color = '#FFFFFF', width = 1)),
               showlegend = FALSE) %>%
    layout(title = name,
           xaxis = list(showgrid = FALSE, zeroline = FALSE, showticklabels = FALSE),
           yaxis = list(showgrid = FALSE, zeroline = FALSE, showticklabels = FALSE))

  export(p, file=paste(output_folder, name, ".pdf", sep = ""))
}