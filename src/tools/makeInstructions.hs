import Text.Regex
import Text.Regex.Posix
import Control.Monad
import Data.Char
import Numeric (readHex, showHex)
import Data.List


main :: IO ()
main =  putStrLn =<< 
         liftM2 (\i d -> javaIS $ mix (parseZ80Instr i) (parseGbDiff d)) 
                readZ80InstrUnparsed gbDiffUnparsed

--------------------------------------------------------------------------------
-- Ugly stuff beneath.

type InstructionSet = [Instruction]
data Instruction = Instruction {
      opcode     :: Int,
      prettyName :: String,
      outputName :: String,
      from       :: [String]
--      cycles     :: Int, -- could be variable
--      arguments  :: [InstrArgument]
      } 

instance Show Instruction where
    show i = showHex (opcode i) (": " ++ outputName i ++ "\t\t\t" ++ prettyName i)

showIS :: InstructionSet -> String
showIS = format . intercalate "\n" . map (\i -> showHex (opcode i) $ ":%s" ++ outputName i ++ "%s" ++ prettyName i)

javaIS :: InstructionSet -> String 
javaIS = intercalate ",\n" . map newInstruction
    where newInstruction i = "new Instruction(0x" 
                             ++ (showHex (opcode i) $ ", \"" 
                             ++ outputName i ++ "\", \""
                             ++ prettyName i ++ "\")")

data InstrArgument = Value 
                   | DoubleValue 
                   | Adress
                   | SignedOffset

z80instrPath = "z80inst2.txt"
gbDiffPath = "instrDiff.txt"

--VALUE ('%', 1), DOUBLE_VALUE ('#', 2), ADDRESS ('&', 2), SIGNEDOFFSET ('¤', 2);
-- n value %, nn double value #, d signed offset ~, f = "0xFF00 + value" $
makeFormatString :: String -> String
makeFormatString = sub "n" "%" 
                   . sub "nn" "#" 
                   . sub "d" "~"
                   . sub "f" "$"

--------------------------------------------------------------------------------
-- Reading from z80instr

readZ80InstrUnparsed :: IO (String)
readZ80InstrUnparsed = readFile z80instrPath

parseZ80Instr :: String -> InstructionSet
parseZ80Instr s = map lineToInstr $ getZ80InstructionLines s

getZ80InstructionLines :: String -> [String]
getZ80InstructionLines s = relevantLines
    where instrLines = lines s
          relevantLines = filter ((=="|[") . take 2) instrLines
          

lineToInstr :: String -> Instruction
lineToInstr s = Instruction { opcode = op
                            , prettyName = pn
                            , outputName = on
                            , from  = splitted
                            }
    where splitted = filter (not . null) . splitRe "\\s*([]|[]+\\s*)+" $ s
          op = fst . head . readHex $ splitted !! 0
          pn = makeFormatString $ splitted !! 2
          on = splitted !! 2

--------------------------------------------------------------------------------
-- Reading diff file

gbDiffUnparsed :: IO (String)
gbDiffUnparsed = readFile gbDiffPath

parseGbDiff :: String -> InstructionSet
parseGbDiff ls = map gbDiffLineToInstr $ getGbDiffInstructionLines ls

getGbDiffInstructionLines :: String -> [String]
getGbDiffInstructionLines = init . drop 4 . lines 

gbDiffLineToInstr :: String -> Instruction
gbDiffLineToInstr l = Instruction {opcode    = op l
                                , prettyName = pn l
                                , outputName = on l
                                , from       = [l]
                                }
    where op = fst . head . readHex . take 2
          nameField = trim . drop 26 . take 43
          -- ändra till unknown för javaboy comp?          
          on l = if (nameField l) == "No operation" 
                  then "NOP" 
                  else parseDiffArguments $ nameField l
          pn = makeFormatString . on
          parseDiffArguments f = sub "offset" "d" 
                                 . sub "word" "nn" 
                                 . sub "byte" "f" 
                                 . sub "C" "0xFF00+C" $ f
          desc = trim . drop 43
          ffOffs l = desc l =~ "FF00+byte"
          cOffs l = desc l =~ "FF00+C"


testDiff = putStrLn =<< liftM (showIS . parseGbDiff) gbDiffUnparsed

--------------------------------------------------------------------------------
-- Put them together

mix :: InstructionSet -> InstructionSet -> InstructionSet
mix a b = map (\(o, i) -> maybe i id (lookup o bz)) az
    where zipop is = zip (map opcode is) is
          az = zipop a
          bz = zipop b



--------------------------------------------------------------------------------
-- String helpers

trim :: String -> String
trim = ltrim . rtrim
    where ltrim = dropWhile (isSpace) 
          rtrim = reverse . ltrim . reverse

splitRe :: String -> String -> [String]
splitRe re [] = []
splitRe re s = l:(splitRe re r)
    where (l, m, r) = s =~ re :: (String, String, String)
                                    

-- formats a string          
format :: String -> String
format s = formatLines --formatLines
    where splitted = map (splitRe "%s") $ lines s
          colwidths = map (foldl (\m l -> max m $ length l) 0) (transpose splitted)
          zipColWidth = map (zip colwidths) splitted
          formatLine = concatMap (\(w, c) -> c ++ (take (3 + w - (length c)) $ repeat ' '))
          formatLines = unlines $ map formatLine zipColWidth
          
sub :: String -> String -> String -> String
sub _ _ "" = ""
sub n s h = if (take (length n) h == n) 
             then s ++ (sub n s (drop (length n) h))
             else (head h : sub n s (tail h))

