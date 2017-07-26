#include <iostream>
#include <string>
#include <fstream>
#include <sstream>
using namespace std;

int main() {
    const string startFilenames[] = {"Stemming_English.txt", "Stemming_French.txt", "Stemming_Spanish.txt"};
    const string endFilenames[] = {"Stemming_English.csv", "Stemming_French.csv", "Stemming_Spanish.csv"};
    const string folderPath = "/home/pawel/AGH/praktyki/git/NewsAnalyzer/SecondProject/Projekt-IO01/FeedsAnalyzer-master";
    fstream inputFile, outputFile;
    string nextLine, basicWord, changedWord;

    for (int i = 0; i < 3; ++i) {
        string inputFilepath = folderPath + "/" + startFilenames[i];
        string outputFilepath = folderPath + "/" + endFilenames[i];

        if (inputFile.is_open()) {
            inputFile.close();
        }
        cout << "Im gonna proccess " << inputFilepath << endl;
        inputFile.open(inputFilepath.c_str(), ios::in | ios::binary);
        if (!inputFile.is_open()) {
            cout << startFilenames[i] << " cannot be opened" << endl;
            continue;
        }
        remove(outputFilepath.c_str());
        outputFile.open(outputFilepath.c_str(), ios::out | ios::binary);
        if (!outputFile.is_open()) {
            cout << outputFilepath << " cannot be created" << endl;
            continue;
        }
        cout << "Lets go" << endl;
        getline(inputFile, nextLine);
        while (!inputFile.eof()) {
            stringstream nextLineStream(nextLine);
            nextLineStream >> basicWord >> changedWord;
            outputFile << basicWord << '\t' << changedWord << '\n';
            getline(inputFile, nextLine);
        }
        cout << "I finished proccessing " << outputFilepath << endl;
        outputFile.close();
        inputFile.close();
    }
}
